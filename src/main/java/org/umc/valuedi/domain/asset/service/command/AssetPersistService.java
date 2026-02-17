package org.umc.valuedi.domain.asset.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 자산 데이터 DB 저장 전용 서비스.
 * AssetFetchService의 self-invocation 문제를 해결하기 위해 분리.
 */
@Service
@RequiredArgsConstructor
public class AssetPersistService {

    private final EntityManager entityManager;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final BankAccountRepository bankAccountRepository;

    private record BankTransactionKey(LocalDateTime trDatetime, Long inAmount, Long outAmount, String desc3) {}
    private record CardApprovalKey(Card card, String approvalNo) {}

    public record SaveResult(int newBankTransactionCount, int newCardApprovalCount) {}

    /**
     * 새로운 자산 데이터를 짧은 트랜잭션에서 필터링 및 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaveResult saveNewAssetData(List<BankTransaction> allFetchedBankTransactions, List<CardApproval> allFetchedCardApprovals) {
        int totalNewBankTransactions = 0;
        if (!allFetchedBankTransactions.isEmpty()) {
            List<BankTransaction> newBankTransactions = filterNewBankTransactions(allFetchedBankTransactions);
            if (!newBankTransactions.isEmpty()) {
                bankTransactionRepository.bulkInsert(newBankTransactions);
                totalNewBankTransactions = newBankTransactions.size();
                updateAccountBalances(newBankTransactions);
            }
        }

        int totalNewCardApprovals = 0;
        if (!allFetchedCardApprovals.isEmpty()) {
            List<CardApproval> newCardApprovals = filterNewCardApprovals(allFetchedCardApprovals);
            if (!newCardApprovals.isEmpty()) {
                cardApprovalRepository.bulkInsert(newCardApprovals);
                totalNewCardApprovals = newCardApprovals.size();
            }
        }

        entityManager.flush();
        entityManager.clear();

        return new SaveResult(totalNewBankTransactions, totalNewCardApprovals);
    }

    private void updateAccountBalances(List<BankTransaction> transactions) {
        Map<BankAccount, BankTransaction> latestTransactions = transactions.stream()
                .collect(Collectors.groupingBy(BankTransaction::getBankAccount,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(BankTransaction::getTrDatetime)),
                                Optional::get
                        )));

        List<BankAccount> updatedAccounts = new ArrayList<>();

        latestTransactions.forEach((account, latestTransaction) -> {
            if (latestTransaction.getAfterBalance() != null) {
                account.updateBalance(latestTransaction.getAfterBalance());
                updatedAccounts.add(account);
            }
        });

        if (!updatedAccounts.isEmpty()) {
            bankAccountRepository.saveAll(updatedAccounts);
        }
    }

    private List<BankTransaction> filterNewBankTransactions(List<BankTransaction> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        LocalDate minDate = allFetched.stream().map(BankTransaction::getTrDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        List<BankAccount> accounts = allFetched.stream().map(BankTransaction::getBankAccount).distinct().toList();

        List<BankTransaction> existingTransactions = bankTransactionRepository.findByBankAccountInAndTrDatetimeAfter(accounts, minDate.atStartOfDay());

        Set<BankTransactionKey> existingKeys = existingTransactions.stream()
                .map(tx -> new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), "")))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(tx -> !existingKeys.contains(new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), ""))))
                .toList();
    }

    private List<CardApproval> filterNewCardApprovals(List<CardApproval> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        List<Card> cards = allFetched.stream().map(CardApproval::getCard).distinct().toList();
        List<String> approvalNos = allFetched.stream().map(CardApproval::getApprovalNo).distinct().toList();

        List<CardApproval> existingApprovals = cardApprovalRepository.findByCardInAndApprovalNoIn(cards, approvalNos);

        Set<CardApprovalKey> existingKeys = existingApprovals.stream()
                .map(ca -> new CardApprovalKey(ca.getCard(), ca.getApprovalNo()))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(ca -> !existingKeys.contains(new CardApprovalKey(ca.getCard(), ca.getApprovalNo())))
                .toList();
    }
}
