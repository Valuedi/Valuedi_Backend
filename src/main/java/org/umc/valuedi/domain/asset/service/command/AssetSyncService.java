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
        List<Card> cards = codefAssetService.getCards(connection);
        
        try {
            cardRepository.saveAll(cards);
        } catch (DataIntegrityViolationException e) {
            log.warn("카드 저장 중 중복 발생 - 개별 저장 시도");
            for (Card card : cards) {
                try {
                    cardRepository.save(card);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 존재하는 카드는 무시
                }
            }
        }
        log.info("보유 카드 목록 동기화 완료 - {}개 카드", cards.size());

        // 전체 승인 내역 조회 및 카드 매칭 후 저장
        if (syncCardApprovals(connection)) {
            syncLedger(connection.getMember());
        }
        
        log.info("카드사 자산 동기화 완료 - Connection ID: {}", connection.getId());
    }

    /**
     * 가계부 동기화 헬퍼 메서드
     */
    private void syncLedger(Member member) {
        ledgerSyncService.syncTransactions(member, LocalDate.now().minusMonths(DEFAULT_SYNC_PERIOD_MONTHS), LocalDate.now());
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
        log.info("카드 승인내역 동기화 시작 - Connection ID: {}", connection.getId());

        // 해당 연동의 모든 카드 목록 조회 (DB)
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        if (cards.isEmpty()) {
            log.warn("연동된 카드가 없어 승인내역 동기화를 건너뜁니다.");
            return false;
        }
        try {

             connection.getCardList().clear();
             connection.getCardList().addAll(cards);
        } catch (Exception e) {
            log.warn("Connection 객체의 카드 리스트 갱신 중 오류 (무시하고 진행): {}", e.getMessage());
        }

        // 전체 승인 내역 조회 (API)
        // CodefAssetService 내부에서 CodefAssetConverter를 통해 매칭까지 완료된 리스트 반환
        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection);
        if (approvals.isEmpty()) {
            log.info("조회된 승인내역이 없습니다.");
            return false;
        }

        // 저장
        cardApprovalRepository.bulkInsert(approvals);
        log.info("카드 승인내역 Bulk Insert 완료 - {}건", approvals.size());
        return true;
    }
}
