package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetFetchService {

    private final CodefConnectionRepository codefConnectionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final CodefAssetService codefAssetService;

    public AssetResDTO.AssetSyncResult fetchAndSaveLatestData(Member member) {
        List<CodefConnection> connections = codefConnectionRepository.findByMemberId(member.getId());
        LocalDate today = LocalDate.now();
        LocalDate overallMinDate = today;

        int totalNewBankTransactions = 0;
        int totalNewCardApprovals = 0;
        List<String> successOrganizations = new ArrayList<>();
        List<String> failureOrganizations = new ArrayList<>();

        for (CodefConnection connection : connections) {
            try {
                if (connection.getBusinessType() == BusinessType.BK) {
                    // 은행 거래 내역 로직 (이전과 동일, 필요시 중복 제거 로직 추가)
                    List<BankAccount> accounts = connection.getBankAccountList();
                    for (BankAccount account : accounts) {
                        LocalDate startDate = bankTransactionRepository.findLatestTransactionDateByAccount(account)
                                .orElse(today.withDayOfMonth(1));
                        
                        List<BankTransaction> fetchedTransactions = codefAssetService.getBankTransactions(connection, account, startDate, today);
                        
                        if (!fetchedTransactions.isEmpty()) {
                            // DB에서 비교할 기존 거래내역 조회 (조회 기간을 최소화하여 성능 확보)
                            List<BankTransaction> existingTransactionsInPeriod = bankTransactionRepository
                                    .findByBankAccountAndTrDatetimeBetween(account, startDate.atStartOfDay(), today.atTime(23, 59, 59));

                            // 빠른 비교를 위해 기존 거래내역의 복합키를 Set으로 변환
                            Set<String> existingTransactionKeys = existingTransactionsInPeriod.stream()
                                    .map(tx -> tx.getTrDatetime().toString() + tx.getInAmount() + tx.getOutAmount() + tx.getDesc3())
                                    .collect(Collectors.toSet());

                            // 새로운 거래 내역만 필터링
                            List<BankTransaction> newTransactions = fetchedTransactions.stream()
                                    .filter(tx -> {
                                        String key = tx.getTrDatetime().toString() + tx.getInAmount() + tx.getOutAmount() + tx.getDesc3();
                                        return !existingTransactionKeys.contains(key);
                                    })
                                    .collect(Collectors.toList());

                            // 새로운 내역이 있을 경우에만 저장
                            if (!newTransactions.isEmpty()) {
                                bankTransactionRepository.saveAll(newTransactions);
                                totalNewBankTransactions += newTransactions.size();
                                if (startDate.isBefore(overallMinDate)) {
                                    overallMinDate = startDate;
                                }
                            }
                        }
                    }
                } else if (connection.getBusinessType() == BusinessType.CD) {
                    LocalDate startDate = cardApprovalRepository.findLatestApprovalDateByMember(member)
                            .orElse(today.withDayOfMonth(1));

                    List<CardApproval> approvals = codefAssetService.getCardApprovals(connection, startDate, today);
                    if (!approvals.isEmpty()) {
                        cardApprovalRepository.saveAll(approvals);
                        totalNewCardApprovals += approvals.size();
                        if (startDate.isBefore(overallMinDate)) {
                            overallMinDate = startDate;
                        }
                    }
                }
                successOrganizations.add(connection.getOrganization());
            } catch (Exception e) {
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
}
