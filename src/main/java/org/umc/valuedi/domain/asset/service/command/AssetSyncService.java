package org.umc.valuedi.domain.asset.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.umc.valuedi.domain.asset.repository.bank.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.asset.repository.card.CardRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AssetSyncService {

    private final CodefAssetService codefAssetService;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardRepository cardRepository;
    private final CardApprovalRepository cardApprovalRepository;
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
        for (BankAccount account : accounts) {
            syncBankTransactions(connection, account);
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
        syncCardApprovals(connection);
        
        log.info("카드사 자산 동기화 완료 - Connection ID: {}", connection.getId());
    }

    /**
     * 특정 계좌의 거래 내역 동기화
     */
    private void syncBankTransactions(CodefConnection connection, BankAccount account) {
        log.info("계좌 거래내역 동기화 시작 - Account: {}", account.getAccountDisplay());
        
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account);
        
        if (transactions.isEmpty()) {
            return;
        }

        bankTransactionRepository.bulkInsert(transactions);
        log.info("계좌 거래내역 Bulk Insert 완료 - {}건", transactions.size());
    }

    /**
     * 카드 승인 내역 동기화 (전체 조회 후 매칭)
     */
    private void syncCardApprovals(CodefConnection connection) {
        log.info("카드 승인내역 동기화 시작 - Connection ID: {}", connection.getId());

        // 해당 연동의 모든 카드 목록 조회 (DB)
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        if (cards.isEmpty()) {
            log.warn("연동된 카드가 없어 승인내역 동기화를 건너뜁니다.");
            return;
        }

        // 전체 승인 내역 조회 (API)
        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection);
        if (approvals.isEmpty()) {
            log.info("조회된 승인내역이 없습니다.");
            return;
        }

        List<CardApproval> matchedApprovals = new ArrayList<>();

        // 매칭 로직
        for (CardApproval approval : approvals) {
            String resCardNo = extractResCardNo(approval);
            if (resCardNo == null) {
                log.warn("승인내역에서 카드번호를 찾을 수 없어 스킵합니다. ApprovalNo: {}", approval.getApprovalNo());
                continue;
            }

            Card matchedCard = findMatchingCard(cards, resCardNo);
            if (matchedCard != null) {
                approval.assignCard(matchedCard);
                matchedApprovals.add(approval);
            } else {
                log.warn("승인내역의 카드번호({})와 일치하는 카드를 찾을 수 없어 스킵합니다.", resCardNo);
            }
        }

        // 저장
        if (!matchedApprovals.isEmpty()) {
            cardApprovalRepository.bulkInsert(matchedApprovals);
            log.info("카드 승인내역 Bulk Insert 완료 - {}건 저장 (총 조회: {}건)", matchedApprovals.size(), approvals.size());
        }
    }

    private String extractResCardNo(CardApproval approval) {
        try {
            JsonNode root = objectMapper.readTree(approval.getRawJson());
            if (root.has("resCardNo")) {
                return root.get("resCardNo").asText();
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    private Card findMatchingCard(List<Card> cards, String resCardNo) {
        // 1순위: 정확히 일치
        for (Card card : cards) {
            if (resCardNo.equals(card.getCardNoMasked())) {
                return card;
            }
        }

        // 2순위: 뒤 4자리 일치
        if (resCardNo.length() >= 4) {
            String last4 = resCardNo.substring(resCardNo.length() - 4);
            for (Card card : cards) {
                String cardNo = card.getCardNoMasked();
                if (cardNo != null && cardNo.length() >= 4 && cardNo.endsWith(last4)) {
                    return card;
                }
            }
        }

        return null;
    }
}
