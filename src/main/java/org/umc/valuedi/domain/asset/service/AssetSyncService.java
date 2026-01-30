package org.umc.valuedi.domain.asset.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.event.AssetRawDataSavedEvent;
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.exception.code.AssetErrorCode;
import org.umc.valuedi.domain.asset.repository.bank.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.asset.repository.card.CardRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSyncService {

    private final CodefAssetService codefAssetService;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardRepository cardRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final ObjectMapper objectMapper;
    private final CodefConnectionRepository codefConnectionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void syncAssets(Long connectionId) {
        CodefConnection connection = codefConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        log.info("자산 동기화 시작 - Connection ID: {}", connection.getId());
        try {
            if (connection.getBusinessType() == BusinessType.BK) {
                syncBankAssets(connection);
            } else if (connection.getBusinessType() == BusinessType.CD) {
                syncCardAssets(connection);
            }
        } catch (CodefException e) {
            // 목록 조회 자체가 실패하면 전체 에러 처리
            log.error("자산 목록 조회 실패 - Connection ID: {}", connection.getId(), e);
            throw new AssetException(AssetErrorCode.ASSET_SYNC_FAILED);
        }
    }

    private void syncBankAssets(CodefConnection connection) {
        // 계좌 목록 조회 및 저장
        List<BankAccount> accountsFromApi = codefAssetService.getBankAccounts(connection);
        List<BankAccount> savedAccounts = saveNewBankAccounts(connection, accountsFromApi);

        // 계좌별 거래내역 동기화 (부분 성공 허용)
        for (BankAccount account : savedAccounts) {
            try {
                syncBankTransactions(connection, account);
            } catch (Exception e) {
                // 이 계좌가 실패해도 다른 계좌는 계속 진행
                log.error("[Partial Fail] 거래내역 동기화 실패 - Account: {}, Error: {}",
                        account.getAccountDisplay(), e.getMessage());
            }
        }
    }

    private List<BankAccount> saveNewBankAccounts(CodefConnection connection, List<BankAccount> accountsFromApi) {
        if (accountsFromApi.isEmpty()) return new ArrayList<>();

        List<BankAccount> existingAccounts = bankAccountRepository.findByCodefConnectionAndIsActiveTrue(connection);

        Set<String> existingNumbers = existingAccounts.stream()
                .map(BankAccount::getAccountDisplay)
                .collect(Collectors.toSet());

        List<BankAccount> newAccounts = accountsFromApi.stream()
                .filter(acc -> !existingNumbers.contains(acc.getAccountDisplay()))
                .collect(Collectors.toList());

        if (!newAccounts.isEmpty()) {
            bankAccountRepository.saveAll(newAccounts);
            log.info("새로운 은행 계좌 {}건 저장 - Connection ID: {}", newAccounts.size(), connection.getId());
        }

        existingAccounts.addAll(newAccounts);
        return existingAccounts;
    }

    private void syncBankTransactions(CodefConnection connection, BankAccount account) {
        // 최초 연동이므로 startDate는 null로 전달 (CodefAssetService에서 기본값 3개월 사용)
        List<BankTransaction> transactions = codefAssetService.getBankTransactions(connection, account, null);
        if (transactions.isEmpty()) {
            return;
        }
        bankTransactionRepository.bulkInsert(transactions);
        log.info("거래내역 저장 완료 - Account: {}, Count: {}", account.getAccountDisplay(), transactions.size());

        // 데이터 저장 후 이벤트 발행
        eventPublisher.publishEvent(new AssetRawDataSavedEvent(connection.getId(), "BK"));

        // 성공 시 해당 계좌의 마지막 동기화 시각 업데이트 (더티 체킹)
        account.updateLastSyncedAt(LocalDateTime.now());
    }

    private void syncCardAssets(CodefConnection connection) {
        // 카드 목록 조회 및 저장
        List<Card> cardsFromApi = codefAssetService.getCards(connection);
        List<Card> savedCards = saveNewCards(connection, cardsFromApi);

        // 승인내역 동기화 (부분 성공 허용)
        try {
            syncCardApprovals(connection, savedCards);
        } catch (Exception e) {
            log.error("[Partial Fail] 카드 승인내역 동기화 실패 - Connection: {}", connection.getId(), e);
        }
    }

    private List<Card> saveNewCards(CodefConnection connection, List<Card> cardsFromApi) {
        if (cardsFromApi.isEmpty()) return new ArrayList<>();

        List<Card> existingCards = cardRepository.findByCodefConnection(connection);
        Set<String> existingNumbers = existingCards.stream()
                .map(Card::getCardNoMasked)
                .collect(Collectors.toSet());

        List<Card> newCards = cardsFromApi.stream()
                .filter(card -> !existingNumbers.contains(card.getCardNoMasked()))
                .collect(Collectors.toList());

        if (!newCards.isEmpty()) {
            cardRepository.saveAll(newCards);
            log.info("새로운 카드 {}건 저장 - Connection ID: {}", newCards.size(), connection.getId());
        }

        existingCards.addAll(newCards);
        return existingCards;
    }

    private void syncCardApprovals(CodefConnection connection, List<Card> cards) {
        if (cards.isEmpty()) return;

        // 최초 연동이므로 startDate는 null로 전달 (CodefAssetService에서 기본값 3개월 사용)
        List<CardApproval> approvals = codefAssetService.getCardApprovals(connection, null);
        if (approvals.isEmpty()) return;

        // 카드 매칭을 위한 Map 최적화
        Map<String, Card> cardMap = cards.stream()
                .collect(Collectors.toMap(Card::getCardNoMasked, Function.identity(), (p1, p2) -> p1));

        List<CardApproval> matchedApprovals = new ArrayList<>();
        for (CardApproval approval : approvals) {
            String resCardNo = extractResCardNo(approval);
            if (resCardNo != null && cardMap.containsKey(resCardNo)) {
                approval.assignCard(cardMap.get(resCardNo));
                matchedApprovals.add(approval);
            } else {
                log.warn("승인내역의 카드번호와 일치하는 카드를 찾을 수 없어 스킵합니다. 카드번호: {}, 승인번호: {}", resCardNo, approval.getApprovalNo());
            }
        }

        if (!matchedApprovals.isEmpty()) {
            cardApprovalRepository.bulkInsert(matchedApprovals);
            log.info("카드 승인내역 저장 완료 - Connection ID: {}, Count: {}", connection.getId(), matchedApprovals.size());

            // 데이터 저장 후 이벤트 발행
            eventPublisher.publishEvent(new AssetRawDataSavedEvent(connection.getId(), "CD"));
        }

        // 성공 시 모든 카드의 동기화 시각 업데이트
        LocalDateTime now = LocalDateTime.now();
        cards.forEach(card -> card.updateLastSyncedAt(now));
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
}
