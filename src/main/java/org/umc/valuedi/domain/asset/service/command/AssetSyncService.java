package org.umc.valuedi.domain.asset.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    /**
     * 금융사 종류에 따라 자산 동기화 분기
     */
    public void syncAssets(CodefConnection connection) {
        if (connection.getBusinessType() == BusinessType.BK) {
            syncBankAssets(connection);
        } else if (connection.getBusinessType() == BusinessType.CD) {
            syncCardAssets(connection);
        }
    }

    /**
     * 은행 관련 자산 동기화 (계좌 목록, 거래 내역)
     */
    private void syncBankAssets(CodefConnection connection) {
        log.info("은행 자산 동기화 시작 - Connection ID: {}", connection.getId());

        // API 호출 (트랜잭션 없음)
        List<BankAccount> accounts = codefAssetService.getBankAccounts(connection);

        // DB 저장 (별도 트랜잭션)
        saveBankAccounts(accounts);
        log.info("보유 계좌 목록 동기화 완료 - {}개 계좌", accounts.size());

        // 동기화된 모든 계좌 조회
        List<BankAccount> allAccounts = bankAccountRepository.findByCodefConnection(connection);

        // 각 계좌별 거래 내역 동기화
        boolean anyUpdated = false;
        for (BankAccount account : allAccounts) {
            if (syncBankTransactionsForAccount(connection, account)) {
                anyUpdated = true;
            }
        }

        // 가계부 동기화 (필요시)
        if (anyUpdated) {
            syncLedger(connection.getMember());
        }

        log.info("은행 자산 동기화 완료 - Connection ID: {}", connection.getId());
    }

    /**
     * 카드 관련 자산 동기화 (카드 목록, 승인 내역)
     */
    private void syncCardAssets(CodefConnection connection) {
        log.info("카드사 자산 동기화 시작 - Connection ID: {}", connection.getId());

        // API 호출 (트랜잭션 없음)
        log.info("[SyncCardAssets] 보유 카드 목록 조회를 시작합니다. Connection ID: {}", connection.getId());
        List<Card> cards = codefAssetService.getCards(connection);
        log.info("[SyncCardAssets] 보유 카드 목록을 조회했습니다. 카드 수: {}, Connection ID: {}", cards.size(), connection.getId());

        // DB 저장 (별도 트랜잭션)
        saveCards(cards);
        log.info("보유 카드 목록 동기화를 완료했습니다. 총 {}개의 카드가 동기화되었습니다.", cards.size());

        // API 호출 및 DB 저장: 카드 승인 내역 동기화
        boolean approvalsSynced = syncCardApprovals(connection);

        // 가계부 동기화 (필요시)
        if (approvalsSynced) {
            log.info("[SyncCardAssets] 가계부 동기화를 시작합니다. Connection ID: {}", connection.getId());
            syncLedger(connection.getMember());
            log.info("[SyncCardAssets] 가계부 동기화를 완료했습니다. Connection ID: {}", connection.getId());
        }

        log.info("카드사 자산 동기화 완료 - Connection ID: {}", connection.getId());
    }

    /**
     * 특정 계좌의 거래 내역 동기화 (트랜잭션 없음)
     */
    private boolean syncBankTransactionsForAccount(CodefConnection connection, BankAccount account) {
        log.info("계좌 거래내역 동기화 시작 - Account: {}", account.getAccountDisplay());

        // API 호출
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account);
        if (transactions.isEmpty()) {
            return false;
        }

        // DB 저장 (별도 트랜잭션)
        saveBankTransactions(transactions);
        return true;
    }

    /**
     * 카드 승인 내역 동기화 (트랜잭션 없음)
     */
    private boolean syncCardApprovals(CodefConnection connection) {
        log.info("카드 승인내역 동기화를 시작합니다. Connection ID: {}", connection.getId());

        // DB 조회: 동기화에 필요한 카드 목록 조회
        log.info("[SyncCardApprovals] DB에서 카드 목록 조회를 시작합니다. Connection ID: {}", connection.getId());
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        log.info("[SyncCardApprovals] DB에서 카드 목록을 조회했습니다. 카드 수: {}, Connection ID: {}", cards.size(), connection.getId());

        if (cards.isEmpty()) {
            log.warn("연동된 카드가 없어 승인내역 동기화를 건너뜁니다. Connection ID: {}", connection.getId());
            return false;
        }

        // API 호출: 3개월치 승인 내역 조회 (트랜잭션 없음)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(DEFAULT_SYNC_PERIOD_MONTHS);

        log.info("[SyncCardApprovals] Codef API를 통해 승인 내역 조회를 시작합니다. (기간: {} ~ {})", startDate, endDate);
        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection, cards, startDate, endDate);
        log.info("[SyncCardApprovals] Codef API를 통해 승인 내역을 조회했습니다. 승인 내역 수: {}, Connection ID: {}", approvals.size(), connection.getId());

        if (approvals.isEmpty()) {
            log.info("조회된 승인내역이 없습니다. Connection ID: {}", connection.getId());
            return false;
        }

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
            log.warn("계좌 저장 중 중복 발생 - 개별 저장 시도");
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
        log.info("계좌 거래내역 Bulk Insert 완료 - {}건", transactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCards(List<Card> cards) {
        if (cards.isEmpty()) return;
        try {
            log.info("[SyncCardAssets] 카드 정보 저장을 시작합니다.");
            cardRepository.saveAll(cards);
            log.info("[SyncCardAssets] 카드 정보를 저장했습니다.");
        } catch (DataIntegrityViolationException e) {
            log.warn("[SyncCardAssets] 카드 저장 중 중복이 발생하여 개별 저장을 시도합니다.");
            for (Card card : cards) {
                try {
                    cardRepository.save(card);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 존재하는 카드는 무시
                }
            }
            log.info("[SyncCardAssets] 카드 정보 개별 저장을 완료했습니다.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCardApprovals(List<CardApproval> approvals) {
        log.info("[SyncCardApprovals] 승인 내역 저장을 시작합니다.");
        cardApprovalRepository.bulkInsert(approvals);
        log.info("[SyncCardApprovals] 승인 내역 저장을 완료했습니다. 총 {}건이 처리되었습니다.", approvals.size());
    }
}
