package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSyncService {

    private static final int DEFAULT_SYNC_PERIOD_MONTHS = 3;

    private final CodefAssetService codefAssetService;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardRepository cardRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final LedgerSyncService ledgerSyncService;

    /**
     * 금융사 종류에 따라 자산 동기화 분기
     */
    public void syncAssets(CodefConnection connection) {
        log.info("[AssetSync] 시작 - Connection ID: {}, Org: {}", connection.getId(), connection.getOrganization());
        
        if (connection.getBusinessType() == BusinessType.BK) {
            syncBankAssets(connection);
        } else if (connection.getBusinessType() == BusinessType.CD) {
            syncCardAssets(connection);
        }
        
        log.info("[AssetSync] 완료 - Connection ID: {}", connection.getId());
    }

    /**
     * 은행 관련 자산 동기화 (계좌 목록, 거래 내역)
     */
    private void syncBankAssets(CodefConnection connection) {
        // API 호출 (트랜잭션 없음)
        List<BankAccount> accounts = codefAssetService.getBankAccounts(connection);
        if (accounts.isEmpty()) return;

        // DB 저장 (별도 트랜잭션)
        saveBankAccounts(accounts);

        // 동기화된 모든 계좌 조회
        List<BankAccount> allAccounts = bankAccountRepository.findByCodefConnection(connection);
        int updatedAccountCount = 0;
        
        for (BankAccount account : allAccounts) {
            if (syncBankTransactionsForAccount(connection, account)) {
                updatedAccountCount++;
            }
        }

        // 가계부 동기화 (필요시)
        if (updatedAccountCount > 0) {
            syncLedger(connection.getMember());
        }
        
        log.info("[AssetSync] 은행 동기화 요약 - 계좌: {}개, 거래내역 업데이트 계좌: {}개", allAccounts.size(), updatedAccountCount);
    }

    /**
     * 카드 관련 자산 동기화 (카드 목록, 승인 내역)
     */
    private void syncCardAssets(CodefConnection connection) {
        // API 호출 (트랜잭션 없음)
        List<Card> cards = codefAssetService.getCards(connection);
        if (cards.isEmpty()) return;

        // DB 저장 (별도 트랜잭션)
        saveCards(cards);

        // API 호출 및 DB 저장: 카드 승인 내역 동기화
        boolean approvalsSynced = syncCardApprovals(connection);

        // 가계부 동기화 (필요시)
        if (approvalsSynced) {
            syncLedger(connection.getMember());
        }
        
        log.info("[AssetSync] 카드 동기화 요약 - 카드: {}개, 승인내역 업데이트: {}", cards.size(), approvalsSynced ? "성공" : "없음");
    }

    /**
     * 특정 계좌의 거래 내역 동기화 (트랜잭션 없음)
     */
    private boolean syncBankTransactionsForAccount(CodefConnection connection, BankAccount account) {
        // API 호출
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account);
        if (transactions.isEmpty()) return false;

        // DB 저장 (별도 트랜잭션)
        saveBankTransactions(transactions);
        return true;
    }

    /**
     * 카드 승인 내역 동기화 (트랜잭션 없음)
     */
    private boolean syncCardApprovals(CodefConnection connection) {
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        if (cards.isEmpty()) return false;

        // API 호출: 3개월치 승인 내역 조회 (트랜잭션 없음)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(DEFAULT_SYNC_PERIOD_MONTHS);

        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection, cards, startDate, endDate);
        if (approvals.isEmpty()) return false;

        // DB 저장 (별도 트랜잭션)
        saveCardApprovals(approvals);
        return true;
    }

    /**
     * 가계부 동기화 헬퍼 메서드
     */
    private void syncLedger(Member member) {
        // LedgerSyncService의 rebuildLedger는 이미 @Transactional 이므로 그대로 호출
        ledgerSyncService.rebuildLedger(member, LocalDate.now().minusMonths(DEFAULT_SYNC_PERIOD_MONTHS), LocalDate.now());
    }

    // --- DB 저장을 위한 트랜잭션 메서드 ---

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBankAccounts(List<BankAccount> accounts) {
        if (accounts.isEmpty()) return;
        try {
            bankAccountRepository.saveAll(accounts);
        } catch (DataIntegrityViolationException e) {
            for (BankAccount account : accounts) {
                try {
                    bankAccountRepository.save(account);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 존재하는 계좌는 무시
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBankTransactions(List<BankTransaction> transactions) {
        bankTransactionRepository.bulkInsert(transactions);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCards(List<Card> cards) {
        if (cards.isEmpty()) return;
        try {
            cardRepository.saveAll(cards);
        } catch (DataIntegrityViolationException e) {
            for (Card card : cards) {
                try {
                    cardRepository.save(card);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 존재하는 카드는 무시
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCardApprovals(List<CardApproval> approvals) {
        cardApprovalRepository.bulkInsert(approvals);
    }
}
