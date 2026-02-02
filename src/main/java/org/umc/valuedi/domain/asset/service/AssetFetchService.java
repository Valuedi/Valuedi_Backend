package org.umc.valuedi.domain.asset.service;

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
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
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
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final AssetFetchWorker assetFetchWorker;

    @Transactional
    public AssetResDTO.AssetSyncResult fetchAndSaveLatestData(Member member) {
        List<CodefConnection> connections = codefConnectionRepository.findByMemberId(member.getId());
        LocalDate today = LocalDate.now();

        // 각 기관별로 비동기 API 호출 실행
        List<CompletableFuture<AssetFetchWorker.FetchResult>> futures = connections.stream()
                .map(connection -> {
                    LocalDate startDate = getStartDateForConnection(connection, member, today);
                    return assetFetchWorker.fetchAndConvertData(connection, member, startDate);
                })
                .collect(Collectors.toList());

        // 모든 비동기 작업이 완료될 때까지 대기하고 결과 취합
        List<AssetFetchWorker.FetchResult> fetchResults = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // --- 여기서부터는 모든 API 호출이 완료된 상태 ---

        int totalNewBankTransactions = 0;
        int totalNewCardApprovals = 0;
        List<String> successOrganizations = new ArrayList<>();
        List<String> failureOrganizations = new ArrayList<>();
        LocalDate overallMinDate = today;

        // 취합된 결과를 바탕으로 DB 저장 및 중복 제거 로직 실행
        for (AssetFetchWorker.FetchResult result : fetchResults) {
            CodefConnection connection = result.connection();
            if (!result.isSuccess()) {
                failureOrganizations.add(connection.getOrganization());
                log.error("[ASSET-FETCH] 자산 데이터 수집 비동기 작업 실패. 기관: {}, 회원 ID: {}", connection.getOrganization(), member.getId());
                continue;
            }

            try {
                if (connection.getBusinessType() == BusinessType.BK && !result.bankTransactions().isEmpty()) {
                    List<BankTransaction> newTransactions = filterNewBankTransactions(result.bankTransactions());
                    if (!newTransactions.isEmpty()) {
                        bankTransactionRepository.saveAll(newTransactions);
                        totalNewBankTransactions += newTransactions.size();
                        if (result.startDate().isBefore(overallMinDate)) {
                            overallMinDate = result.startDate();
                        }
                    }
                } else if (connection.getBusinessType() == BusinessType.CD && !result.cardApprovals().isEmpty()) {
                    List<CardApproval> newApprovals = filterNewCardApprovals(result.cardApprovals());
                    if (!newApprovals.isEmpty()) {
                        cardApprovalRepository.saveAll(newApprovals);
                        totalNewCardApprovals += newApprovals.size();
                        if (result.startDate().isBefore(overallMinDate)) {
                            overallMinDate = result.startDate();
                        }
                    }
                }
                successOrganizations.add(connection.getOrganization());
            } catch (Exception e) {
                log.error("[ASSET-DB-SAVE] 자산 데이터 저장 중 오류 발생. 기관: {}, 회원 ID: {}", connection.getOrganization(), member.getId(), e);
                failureOrganizations.add(connection.getOrganization());
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

    private LocalDate getStartDateForConnection(CodefConnection connection, Member member, LocalDate today) {
        if (connection.getBusinessType() == BusinessType.BK) {
            // 기관에 연결된 모든 계좌의 마지막 거래일 중 가장 오래된 날짜를 찾거나, 없으면 월초로 설정
            return connection.getBankAccountList().stream()
                    .map(bankTransactionRepository::findLatestTransactionDateByAccount)
                    .flatMap(Optional::stream)
                    .min(LocalDate::compareTo)
                    .orElse(today.withDayOfMonth(1));
        } else if (connection.getBusinessType() == BusinessType.CD) {
            return cardApprovalRepository.findLatestApprovalDateByMember(member)
                    .orElse(today.withDayOfMonth(1));
        }
        return today.withDayOfMonth(1);
    }

    private List<BankTransaction> filterNewBankTransactions(List<BankTransaction> fetchedTransactions) {
        Map<BankAccount, List<BankTransaction>> transactionsByAccount = fetchedTransactions.stream()
                .filter(tx -> tx.getBankAccount() != null)
                .collect(Collectors.groupingBy(BankTransaction::getBankAccount));

        List<BankTransaction> allNewTransactions = new ArrayList<>();

        for (Map.Entry<BankAccount, List<BankTransaction>> entry : transactionsByAccount.entrySet()) {
            BankAccount account = entry.getKey();
            List<BankTransaction> transactionsForAccount = entry.getValue();

            // 복합키를 기반으로 중복 제거
            Set<String> existingKeys = bankTransactionRepository
                    .findByBankAccountAndTrDatetimeBetween(account, transactionsForAccount.get(0).getTrDate().atStartOfDay(), LocalDate.now().atTime(23, 59, 59))
                    .stream()
                    .map(tx -> tx.getTrDatetime().toString() + tx.getInAmount() + tx.getOutAmount() + tx.getDesc3())
                    .collect(Collectors.toSet());

            List<BankTransaction> newTransactions = transactionsForAccount.stream()
                    .filter(tx -> {
                        String key = tx.getTrDatetime().toString() + tx.getInAmount() + tx.getOutAmount() + tx.getDesc3();
                        return !existingKeys.contains(key);
                    })
                    .collect(Collectors.toList());
            
            allNewTransactions.addAll(newTransactions);
        }
        return allNewTransactions;
    }

    private List<CardApproval> filterNewCardApprovals(List<CardApproval> fetchedApprovals) {
        Map<Card, List<CardApproval>> approvalsByCard = fetchedApprovals.stream()
                .filter(approval -> approval.getCard() != null)
                .collect(Collectors.groupingBy(CardApproval::getCard));

        List<CardApproval> allNewApprovals = new ArrayList<>();

        for (Map.Entry<Card, List<CardApproval>> entry : approvalsByCard.entrySet()) {
            Card card = entry.getKey();
            List<CardApproval> approvalsForCard = entry.getValue();
            List<String> approvalNos = approvalsForCard.stream().map(CardApproval::getApprovalNo).collect(Collectors.toList());
            Set<String> existingApprovalNos = cardApprovalRepository.findExistingApprovalNosByCard(card, approvalNos);
            List<CardApproval> newApprovals = approvalsForCard.stream()
                    .filter(approval -> !existingApprovalNos.contains(approval.getApprovalNo()))
                    .collect(Collectors.toList());
            allNewApprovals.addAll(newApprovals);
        }
        return allNewApprovals;
    }
}
