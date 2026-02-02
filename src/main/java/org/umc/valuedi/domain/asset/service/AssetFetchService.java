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
                    List<BankAccount> accounts = connection.getBankAccountList();
                    for (BankAccount account : accounts) {
                        LocalDate startDate = bankTransactionRepository.findLatestTransactionDateByAccount(account)
                                .orElse(today.withDayOfMonth(1));

                        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account, startDate, today);
                        if (!transactions.isEmpty()) {
                            bankTransactionRepository.saveAll(transactions);
                            totalNewBankTransactions += transactions.size();
                            if (startDate.isBefore(overallMinDate)) {
                                overallMinDate = startDate;
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
