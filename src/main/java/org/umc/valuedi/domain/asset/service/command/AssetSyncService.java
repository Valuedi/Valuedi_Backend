package org.umc.valuedi.domain.asset.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;
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
@Transactional
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
        
        // 보유 계좌 목록 조회 및 저장
        List<BankAccount> accounts = codefAssetService.getBankAccounts(connection);
        
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
        log.info("보유 계좌 목록 동기화 완료 - {}개 계좌", accounts.size());

        // 각 계좌별 거래 내역 조회 및 저장
        boolean anyUpdated = false;
        for (BankAccount account : accounts) {
            if (syncBankTransactions(connection, account)) {
                anyUpdated = true;
            }
        }

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
        
        // 보유 카드 목록 조회 및 저장
        log.info("[SyncCardAssets] 보유 카드 목록 조회를 시작합니다. Connection ID: {}", connection.getId());
        List<Card> cards = codefAssetService.getCards(connection);
        log.info("[SyncCardAssets] 보유 카드 목록을 조회했습니다. 카드 수: {}, Connection ID: {}", cards.size(), connection.getId());
        
        try {
            log.info("[SyncCardAssets] 카드 정보 저장을 시작합니다. Connection ID: {}", connection.getId());
            cardRepository.saveAll(cards);
            log.info("[SyncCardAssets] 카드 정보를 저장했습니다. Connection ID: {}", connection.getId());
        } catch (DataIntegrityViolationException e) {
            log.warn("[SyncCardAssets] 카드 저장 중 중복이 발생하여 개별 저장을 시도합니다. Connection ID: {}", connection.getId());
            for (Card card : cards) {
                try {
                    cardRepository.save(card);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 존재하는 카드는 무시
                }
            }
            log.info("[SyncCardAssets] 카드 정보 개별 저장을 완료했습니다. Connection ID: {}", connection.getId());
        }
        log.info("보유 카드 목록 동기화를 완료했습니다. 총 {}개의 카드가 동기화되었습니다.", cards.size());

        // 전체 승인 내역 조회 및 카드 매칭 후 저장
        log.info("[SyncCardAssets] 카드 승인 내역 동기화를 시작합니다. Connection ID: {}", connection.getId());
        if (syncCardApprovals(connection)) {
            log.info("[SyncCardAssets] 가계부 동기화를 시작합니다. Connection ID: {}", connection.getId());
            syncLedger(connection.getMember());
            log.info("[SyncCardAssets] 가계부 동기화를 완료했습니다. Connection ID: {}", connection.getId());
        }
        
        log.info("카드사 자산 동기화를 완료했습니다. Connection ID: {}", connection.getId());
    }

    /**
     * 가계부 동기화 헬퍼 메서드
     */
    private void syncLedger(Member member) {
        // 기존 syncTransactions 대신 rebuildLedger 호출
        // 범위: 최근 3개월 (기존 정책 유지)
        ledgerSyncService.rebuildLedger(member, LocalDate.now().minusMonths(DEFAULT_SYNC_PERIOD_MONTHS), LocalDate.now());
    }

    /**
     * 특정 계좌의 거래 내역 동기화
     */
    private boolean syncBankTransactions(CodefConnection connection, BankAccount account) {
        log.info("계좌 거래내역 동기화 시작 - Account: {}", account.getAccountDisplay());
        
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account);
        
        if (transactions.isEmpty()) {
            return false;
        }

        bankTransactionRepository.bulkInsert(transactions);
        log.info("계좌 거래내역 Bulk Insert 완료 - {}건", transactions.size());
        return true;
    }

    /**
     * 카드 승인 내역 동기화 (전체 조회 후 매칭)
     */
    private boolean syncCardApprovals(CodefConnection connection) {
        log.info("카드 승인내역 동기화를 시작합니다. Connection ID: {}", connection.getId());

        // 해당 연동의 모든 카드 목록 조회 (DB)
        log.info("[SyncCardApprovals] DB에서 카드 목록 조회를 시작합니다. Connection ID: {}", connection.getId());
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        log.info("[SyncCardApprovals] DB에서 카드 목록을 조회했습니다. 카드 수: {}, Connection ID: {}", cards.size(), connection.getId());

        if (cards.isEmpty()) {
            log.warn("연동된 카드가 없어 승인내역 동기화를 건너뜁니다. Connection ID: {}", connection.getId());
            return false;
        }
        try {
             connection.getCardList().clear();
             connection.getCardList().addAll(cards);
        } catch (Exception e) {
            log.warn("Connection 객체의 카드 리스트 갱신 중 오류가 발생했지만, 진행합니다. 오류: {}, Connection ID: {}", e.getMessage(), connection.getId());
        }

        // 전체 승인 내역 조회 (API)
        // CodefAssetService 내부에서 CodefAssetConverter를 통해 매칭까지 완료된 리스트 반환
        log.info("[SyncCardApprovals] Codef API를 통해 승인 내역 조회를 시작합니다. Connection ID: {}", connection.getId());
        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection);
        log.info("[SyncCardApprovals] Codef API를 통해 승인 내역을 조회했습니다. 승인 내역 수: {}, Connection ID: {}", approvals.size(), connection.getId());

        if (approvals.isEmpty()) {
            log.info("조회된 승인내역이 없습니다. Connection ID: {}", connection.getId());
            return false;
        }

        // 저장
        log.info("[SyncCardApprovals] 승인 내역 저장을 시작합니다. Connection ID: {}", connection.getId());
        cardApprovalRepository.bulkInsert(approvals);
        log.info("[SyncCardApprovals] 승인 내역 저장을 완료했습니다. Connection ID: {}", connection.getId());
        log.info("카드 승인내역 Bulk Insert를 완료했습니다. 총 {}건이 처리되었습니다.", approvals.size());
        return true;
    }
}
