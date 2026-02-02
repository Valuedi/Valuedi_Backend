package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.asset.service.command.worker.AssetFetchWorker;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetFetchService {

    private final CodefConnectionRepository codefConnectionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final AssetFetchWorker assetFetchWorker;

    private record BankTransactionKey(LocalDateTime trDatetime, Long inAmount, Long outAmount, String desc3) {}
    private record CardApprovalKey(Card card, String approvalNo) {}

    @Transactional
    public AssetResDTO.AssetSyncResult fetchAndSaveLatestData(Member member) {
        List<CodefConnection> connections = codefConnectionRepository.findByMemberId(member.getId());
        LocalDate today = LocalDate.now();

        // 각 기관별로 비동기 API 호출 실행
        List<CompletableFuture<AssetFetchWorker.FetchResult>> futures = connections.stream()
                .map(connection -> assetFetchWorker.fetchAndConvertData(connection, member))
                .collect(Collectors.toList());

        // 모든 비동기 작업이 완료될 때까지 대기하고 결과 취합
        List<AssetFetchWorker.FetchResult> fetchResults = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 모든 거래내역을 한번에 조회하기 위한 준비
        List<BankTransaction> allFetchedBankTransactions = new ArrayList<>();
        List<CardApproval> allFetchedCardApprovals = new ArrayList<>();
        
        List<String> successOrganizations = new ArrayList<>();
        List<String> failureOrganizations = new ArrayList<>();
        LocalDate overallMinDate = today;

        // 취합된 결과를 바탕으로 DB 저장 및 중복 제거 로직 실행
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

        // 새로운 데이터 필터링 및 저장
        int totalNewBankTransactions = 0;
        if (!allFetchedBankTransactions.isEmpty()) {
            List<BankTransaction> newBankTransactions = filterNewBankTransactions(allFetchedBankTransactions);
            if (!newBankTransactions.isEmpty()) {
                bankTransactionRepository.saveAll(newBankTransactions);
                totalNewBankTransactions = newBankTransactions.size();
            }
        }

        int totalNewCardApprovals = 0;
        if (!allFetchedCardApprovals.isEmpty()) {
            List<CardApproval> newCardApprovals = filterNewCardApprovals(allFetchedCardApprovals);
            if (!newCardApprovals.isEmpty()) {
                cardApprovalRepository.saveAll(newCardApprovals);
                totalNewCardApprovals = newCardApprovals.size();
            }
        }

        return AssetResDTO.AssetSyncResult.builder()
                .newBankTransactionCount(totalNewBankTransactions)
                .newCardApprovalCount(totalNewCardApprovals)
                .successOrganizations(successOrganizations)
                .failureOrganizations(failureOrganizations)
                .fromDate(overallMinDate)
                .toDate(today)
                .build();
    }

    private List<BankTransaction> filterNewBankTransactions(List<BankTransaction> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        LocalDate minDate = allFetched.stream().map(BankTransaction::getTrDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        List<BankAccount> accounts = allFetched.stream().map(BankTransaction::getBankAccount).distinct().collect(Collectors.toList());
        
        List<BankTransaction> existingTransactions = bankTransactionRepository.findByBankAccountInAndTrDatetimeAfter(accounts, minDate.atStartOfDay());

        Set<BankTransactionKey> existingKeys = existingTransactions.stream()
                .map(tx -> new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), "")))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(tx -> !existingKeys.contains(new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), ""))))
                .collect(Collectors.toList());
    }

    private List<CardApproval> filterNewCardApprovals(List<CardApproval> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        List<Card> cards = allFetched.stream().map(CardApproval::getCard).distinct().collect(Collectors.toList());
        List<String> approvalNos = allFetched.stream().map(CardApproval::getApprovalNo).distinct().collect(Collectors.toList());

        List<CardApproval> existingApprovals = cardApprovalRepository.findByCardInAndApprovalNoIn(cards, approvalNos);

        Set<CardApprovalKey> existingKeys = existingApprovals.stream()
                .map(ca -> new CardApprovalKey(ca.getCard(), ca.getApprovalNo()))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(ca -> !existingKeys.contains(new CardApprovalKey(ca.getCard(), ca.getApprovalNo())))
                .collect(Collectors.toList());
    }
}
