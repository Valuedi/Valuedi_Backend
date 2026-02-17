package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.service.command.worker.AssetFetchWorker;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetFetchService {

    private final CodefConnectionRepository codefConnectionRepository;
    private final AssetFetchWorker assetFetchWorker;
    private final AssetPersistService assetPersistService;

    /**
     * 외부 API 호출 + DB 저장을 분리하여 커넥션 풀 점유 최소화
     */
    public AssetResDTO.AssetSyncResult fetchAndSaveLatestData(Member member) {
        List<CodefConnection> connections = codefConnectionRepository.findByMemberIdWithMember(member.getId());
        LocalDate today = LocalDate.now();

        // 각 기관별로 비동기 API 호출 실행
        List<CompletableFuture<AssetFetchWorker.FetchResult>> futures = connections.stream()
                .map(connection -> assetFetchWorker.fetchAndConvertData(connection, member))
                .toList();

        // 모든 비동기 작업이 완료될 때까지 대기하고 결과 취합
        List<AssetFetchWorker.FetchResult> fetchResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        List<BankTransaction> allFetchedBankTransactions = new ArrayList<>();
        List<CardApproval> allFetchedCardApprovals = new ArrayList<>();

        List<String> successOrganizations = new ArrayList<>();
        List<String> failureOrganizations = new ArrayList<>();
        LocalDate overallMinDate = today;

        for (AssetFetchWorker.FetchResult result : fetchResults) {
            if (result.isSuccess()) {
                successOrganizations.add(result.connection().getOrganization());
                allFetchedBankTransactions.addAll(result.bankTransactions());
                allFetchedCardApprovals.addAll(result.cardApprovals());
                if (result.startDate().isBefore(overallMinDate)) {
                    overallMinDate = result.startDate();
                }
            } else {
                failureOrganizations.add(result.connection().getOrganization());
                log.error("[ASSET-FETCH] 자산 데이터 수집 비동기 작업 실패. 기관: {}, 회원 ID: {}", result.connection().getOrganization(), member.getId());
            }
        }

        Map<Long, Long> realTimeBalances = new HashMap<>();

        // 새로운 데이터 필터링 및 저장
        int totalNewBankTransactions = 0;
        int totalNewCardApprovals = 0;

        if (!allFetchedBankTransactions.isEmpty() || !allFetchedCardApprovals.isEmpty()) {
            AssetPersistService.SaveResult saveResult = assetPersistService.saveNewAssetData(allFetchedBankTransactions, allFetchedCardApprovals);
            totalNewBankTransactions = saveResult.newBankTransactionCount();
            totalNewCardApprovals = saveResult.newCardApprovalCount();
        }

        // 실시간 잔액 추출
        if (!allFetchedBankTransactions.isEmpty()) {
            allFetchedBankTransactions.stream()
                    .collect(Collectors.groupingBy(tx -> tx.getBankAccount().getId(),
                            Collectors.maxBy(Comparator.comparing(BankTransaction::getTrDatetime))))
                    .forEach((accountId, optTx) -> optTx.ifPresent(tx -> {
                        if (tx.getAfterBalance() != null) {
                            realTimeBalances.put(accountId, tx.getAfterBalance());
                        }
                    }));
        }

        return AssetResDTO.AssetSyncResult.builder()
                .newBankTransactionCount(totalNewBankTransactions)
                .newCardApprovalCount(totalNewCardApprovals)
                .successOrganizations(successOrganizations)
                .failureOrganizations(failureOrganizations)
                .fromDate(overallMinDate)
                .toDate(today)
                .latestBalances(realTimeBalances) // 실시간 잔액 데이터 전달
                .build();
    }
}
