package org.umc.valuedi.domain.asset.service;

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
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.exception.code.AssetErrorCode;
import org.umc.valuedi.domain.asset.repository.bank.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.asset.repository.card.CardRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.global.external.codef.exception.CodefException;
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

    public void syncAssets(CodefConnection connection) {
        log.info("자산 동기화 시작 - Connection ID: {}, Type: {}", connection.getId(), connection.getBusinessType());
        try {
            if (connection.getBusinessType() == BusinessType.BK) {
                syncBankAssets(connection);
            } else if (connection.getBusinessType() == BusinessType.CD) {
                syncCardAssets(connection);
            }
            log.info("자산 동기화 완료 - Connection ID: {}", connection.getId());
        } catch (CodefException e) {
            log.error("자산 동기화 중 CODEF 오류 발생 - Connection ID: {}", connection.getId(), e);
            throw new AssetException(AssetErrorCode.ASSET_SYNC_FAILED);
        }
    }

    private void syncBankAssets(CodefConnection connection) {
        List<BankAccount> accounts = codefAssetService.getBankAccounts(connection);
        
        try {
            bankAccountRepository.saveAll(accounts);
        } catch (DataIntegrityViolationException e) {
            log.warn("계좌 저장 중 중복 발생, 개별 저장 시도 - Connection ID: {}", connection.getId());
            for (BankAccount account : accounts) {
                try {
                    bankAccountRepository.save(account);
                } catch (DataIntegrityViolationException ex) {
                    // Ignore duplicate
                }
            }
        }

        for (BankAccount account : accounts) {
            syncBankTransactions(connection, account);
        }
    }

    private void syncCardAssets(CodefConnection connection) {
        List<Card> cards = codefAssetService.getCards(connection);
        
        try {
            cardRepository.saveAll(cards);
        } catch (DataIntegrityViolationException e) {
            log.warn("카드 저장 중 중복 발생, 개별 저장 시도 - Connection ID: {}", connection.getId());
            for (Card card : cards) {
                try {
                    cardRepository.save(card);
                } catch (DataIntegrityViolationException ex) {
                    // Ignore duplicate
                }
            }
        }
        syncCardApprovals(connection);
    }

    private void syncBankTransactions(CodefConnection connection, BankAccount account) {
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account);
        if (transactions.isEmpty()) {
            return;
        }
        bankTransactionRepository.bulkInsert(transactions);
        log.info("계좌 거래내역 저장 완료 - Account: {}, Count: {}", account.getAccountDisplay(), transactions.size());
    }

    private void syncCardApprovals(CodefConnection connection) {
        List<Card> cards = cardRepository.findByCodefConnection(connection);
        if (cards.isEmpty()) {
            return;
        }

        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection);
        if (approvals.isEmpty()) {
            return;
        }

        List<CardApproval> matchedApprovals = new ArrayList<>();

        for (CardApproval approval : approvals) {
            String resCardNo = extractResCardNo(approval);
            if (resCardNo == null) {
                continue;
            }

            Card matchedCard = findMatchingCard(cards, resCardNo);
            if (matchedCard != null) {
                approval.assignCard(matchedCard);
                matchedApprovals.add(approval);
            }
        }

        if (!matchedApprovals.isEmpty()) {
            cardApprovalRepository.bulkInsert(matchedApprovals);
            log.info("카드 승인내역 저장 완료 - Connection ID: {}, Count: {}", connection.getId(), matchedApprovals.size());
        }
    }

    private String extractResCardNo(CardApproval approval) {
        try {
            JsonNode root = objectMapper.readTree(approval.getRawJson());
            if (root.has("resCardNo")) {
                return root.get("resCardNo").asText();
            }
        } catch (JsonProcessingException e) {
            throw new AssetException(AssetErrorCode.ASSET_JSON_PARSING_ERROR);
        }
        return null;
    }

    private Card findMatchingCard(List<Card> cards, String resCardNo) {
        try {
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
        } catch (Exception e) {
            throw new AssetException(AssetErrorCode.ASSET_MATCHING_FAILED);
        }
        return null;
    }
}
