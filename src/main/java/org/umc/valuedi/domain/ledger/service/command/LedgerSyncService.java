package org.umc.valuedi.domain.ledger.service.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.asset.enums.TransactionDirection;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.ledger.dto.request.LedgerSyncRequest;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.CategoryKeyword;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.enums.TransactionType;
import org.umc.valuedi.domain.ledger.exception.LedgerException;
import org.umc.valuedi.domain.ledger.exception.code.LedgerErrorCode;
import org.umc.valuedi.domain.ledger.repository.CategoryKeywordRepository;
import org.umc.valuedi.domain.ledger.repository.CategoryRepository;
import org.umc.valuedi.domain.ledger.repository.LedgerEntryRepository;
import org.umc.valuedi.domain.ledger.utill.LedgerCanonicalKeyUtil;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerSyncService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryKeywordRepository categoryKeywordRepository;
    private final MemberRepository memberRepository;

    // --- 키워드 상수 정의 ---
    private static final List<String> CARD_PAYMENT_KEYWORDS = List.of(
            "체크카드", "직불카드", "카드결제", "카드 승인",
            "승인", "승인취소", "POS", "VAN",
            "BC카드", "신한카드", "국민카드", "삼성카드", "현대카드", "NH카드",
            "CARD", "DEBIT", "체크", "카드"
    );

    private static final List<String> CARD_SETTLEMENT_KEYWORDS = List.of(
            "카드대금", "신용카드대금", "카드청구", "카드자동이체"
    );

    // 메모리 캐시 (키워드 -> 카테고리)
    private volatile Map<String, Category> keywordCache;

    @PostConstruct
    public void init() {
        refreshKeywordCache();
    }

    public void refreshKeywordCache() {
        List<CategoryKeyword> keywords = categoryKeywordRepository.findAllWithCategory();
        this.keywordCache = keywords.stream().collect(Collectors.toMap(CategoryKeyword::getKeyword, CategoryKeyword::getCategory, (e, r) -> e));
    }

    /**
     * 기간 내 가계부 데이터를 완전히 재생성(Rebuild)합니다.
     * 1. 해당 기간 LedgerEntry 삭제
     * 2. 카드/은행 데이터 조회
     * 3. 중복 제거(카드 우선) 후 생성
     */
    @Transactional
    public void rebuildLedger(Member member, LocalDate from, LocalDate to) {
        LocalDateTime startDateTime = from.atStartOfDay();
        LocalDateTime endDateTime = to.plusDays(1).atStartOfDay();

        // 1. 기존 데이터 삭제
        ledgerEntryRepository.deleteByMemberAndTransactionAtBetween(member, startDateTime, endDateTime);
        ledgerEntryRepository.flush(); // 즉시 반영

        // 2. 원천 데이터 조회
        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from, to);
        List<BankTransaction> banks = bankTransactionRepository.findByTrDateBetween(from, to);

        // 3. 카테고리 준비
        Category defaultCategory = categoryRepository.findByCode("ETC").orElseThrow();
        Category transferCategory = categoryRepository.findByCode("TRANSFER").orElseThrow();

        // 4. 병합 및 중복 제거 (Key: CanonicalKey)
        // LinkedHashMap을 사용하여 시간 순서(대략적) 유지 가능성 높임 (실제 정렬은 DB 조회 순서 의존)
        Map<String, LedgerEntry> ledgerMap = new LinkedHashMap<>();

        // 4-1. 카드 데이터 우선 처리
        for (CardApproval ca : cards) {
            if (ca.getUsedDatetime() == null) continue;
            String key = LedgerCanonicalKeyUtil.from(ca);

            // 이미 같은 키가 있다면(매우 드문 경우), 먼저 들어온 것 유지 or 덮어쓰기. 여기선 덮어쓰기
            ledgerMap.put(key, createFromCard(member, ca, defaultCategory, key));
        }

        // 4-2. 은행 데이터 처리 (키 중복 시 Skip -> 즉, 카드가 있으면 은행 무시)
        for (BankTransaction bt : banks) {
            if (bt.getTrDatetime() == null) continue;
            String key = LedgerCanonicalKeyUtil.from(bt);

            if (!ledgerMap.containsKey(key)) {
                ledgerMap.put(key, createFromBank(member, bt, defaultCategory, transferCategory, key));
            } else {
                // 중복 발생 (은행 거래가 카드 승인으로 이미 처리됨)
                log.debug("Duplicate transaction skipped (Bank): {} / Key: {}", bt.getId(), key);
            }
        }

        // 5. 저장
        if (!ledgerMap.isEmpty()) {
            List<LedgerEntry> entries = new ArrayList<>(ledgerMap.values());
            ledgerEntryRepository.bulkInsert(entries);
            log.info("Ledger Rebuild Complete: Member {}, {} entries created.", member.getId(), entries.size());
        }
    }

    private LedgerEntry createFromCard(Member member, CardApproval ca, Category defaultCategory, String key) {
        String merchantName = ca.getMerchantName();
        Category category = mapCategoryByKeyword(ca.getMerchantType(), null);
        if (category == null) category = mapCategoryByKeyword(merchantName, defaultCategory);
        if (category == null) category = defaultCategory;

        TransactionType type = ca.getCancelYn() == CancelStatus.NORMAL ? TransactionType.EXPENSE : TransactionType.INCOME;

        return LedgerEntry.builder()
                .member(member)
                .cardApproval(ca)
                .category(category)
                .title(merchantName == null || merchantName.isBlank() ? "카드 승인" : merchantName)
                .transactionAt(ca.getUsedDatetime())
                .transactionType(type)
                .canonicalKey(key)
                .sourceType("CARD")
                .build();
    }

    private LedgerEntry createFromBank(Member member, BankTransaction bt, Category defaultCategory, Category transferCategory, String key) {
        String combinedDesc = Stream.of(bt.getDesc2(), bt.getDesc3(), bt.getDesc4())
                .filter(Objects::nonNull).collect(Collectors.joining(" "));

        Category category;
        TransactionType type;

        if (isCardSettlement(combinedDesc)) {
            category = transferCategory;
            type = TransactionType.EXPENSE;
        } else {
            category = mapCategoryByKeyword(combinedDesc, defaultCategory);
            type = bt.getDirection() == TransactionDirection.IN ? TransactionType.INCOME : TransactionType.EXPENSE;
        }

        String title = combinedDesc.isEmpty() ? "은행 거래" : combinedDesc;
        if (title.length() > 50) title = title.substring(0, 50);

        return LedgerEntry.builder()
                .member(member)
                .bankTransaction(bt)
                .category(category)
                .title(title)
                .transactionAt(bt.getTrDatetime())
                .transactionType(type)
                .canonicalKey(key)
                .sourceType("BANK")
                .build();
    }

    @Transactional
    public void syncTransactions(Long memberId, LedgerSyncRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new LedgerException(MemberErrorCode.MEMBER_NOT_FOUND));
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();
        syncTransactions(member, from, to);
    }

    @Transactional
    public void syncTransactions(Member member, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new LedgerException(LedgerErrorCode.INVALID_DATE_RANGE);
        }

        Category defaultCategory = categoryRepository.findByCode("ETC")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));
        Category transferCategory = categoryRepository.findByCode("TRANSFER")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));

        // 중복 체크를 위한 카드 내역 (은행 거래와 비교용)
        List<CardApproval> cards = cardApprovalRepository.findMemberCardApprovalsBetween(member.getId(), from.minusDays(1), to.plusDays(1));

        List<LedgerEntry> allNewEntries = new ArrayList<>();
        syncCardApprovals(member, from, to, defaultCategory, allNewEntries);
        syncBankTransactions(member, from, to, cards, defaultCategory, transferCategory, allNewEntries);

        if (!allNewEntries.isEmpty()) {
            ledgerEntryRepository.bulkInsert(allNewEntries);
        }
    }

    private void syncCardApprovals(Member member, LocalDate from, LocalDate to, Category defaultCategory, List<LedgerEntry> allNewEntries) {
        // 가계부에 없는 카드 내역만 조회
        List<CardApproval> cards = cardApprovalRepository.findUnsyncedCardApprovals(member.getId(), from, to);
        if (cards.isEmpty()) return;

        for (CardApproval ca : cards) {
            if (ca.getUsedDatetime() == null) continue;

            String merchantName = ca.getMerchantName();
            Category category = mapCategoryByKeyword(ca.getMerchantType(), null);
            if (category == null) category = mapCategoryByKeyword(merchantName, defaultCategory);
            if (category == null) category = defaultCategory;

            TransactionType transactionType = ca.getCancelYn() == CancelStatus.NORMAL ? TransactionType.EXPENSE : TransactionType.INCOME;

            String key = LedgerCanonicalKeyUtil.from(ca);

            allNewEntries.add(LedgerEntry.builder()
                    .member(member).cardApproval(ca).category(category)
                    .title(merchantName == null || merchantName.isBlank() ? "카드 승인" : merchantName)
                    .transactionAt(ca.getUsedDatetime()).transactionType(transactionType)
                    .canonicalKey(key)
                    .sourceType("CARD")
                    .build());
        }
    }

    private void syncBankTransactions(Member member, LocalDate from, LocalDate to, List<CardApproval> cards, Category defaultCategory, Category transferCategory, List<LedgerEntry> allNewEntries) {
        // 가계부에 없는 은행 내역만 조회
        List<BankTransaction> banks = bankTransactionRepository.findUnsyncedBankTransactions(member.getId(), from, to);
        if (banks.isEmpty()) return;

        for (BankTransaction bt : banks) {
            if (bt.getTrDatetime() == null) continue;

            String combinedDesc = Stream.of(bt.getDesc2(), bt.getDesc3(), bt.getDesc4()).filter(Objects::nonNull).collect(Collectors.joining(" "));
            if (isDuplicateOfCardApproval(bt, combinedDesc, cards)) continue;

            Category category;
            TransactionType transactionType;
            String title = combinedDesc.isEmpty() ? "은행 거래" : combinedDesc;
            if (title.length() > 50) title = title.substring(0, 50);

            if (isCardSettlement(combinedDesc)) {
                category = transferCategory;
                transactionType = TransactionType.EXPENSE;
            } else {
                category = mapCategoryByKeyword(combinedDesc, defaultCategory);
                transactionType = bt.getDirection() == TransactionDirection.IN ? TransactionType.INCOME : TransactionType.EXPENSE;
            }

            String key = LedgerCanonicalKeyUtil.from(bt);

            allNewEntries.add(LedgerEntry.builder()
                    .member(member).bankTransaction(bt).category(category)
                    .title(title).transactionAt(bt.getTrDatetime()).transactionType(transactionType)
                    .canonicalKey(key)
                    .sourceType("BANK")
                    .build());
        }
    }

    private boolean isCardSettlement(String text) {
        return text != null && CARD_SETTLEMENT_KEYWORDS.stream().anyMatch(text::contains);
    }

    private Category mapCategoryByKeyword(String text, Category defaultCategory) {
        if (text == null || text.isEmpty()) return defaultCategory;
        for (Map.Entry<String, Category> entry : keywordCache.entrySet()) {
            if (text.contains(entry.getKey())) return entry.getValue();
        }
        return defaultCategory;
    }

    private boolean isDuplicateOfCardApproval(BankTransaction bt, String combinedDesc, List<CardApproval> cards) {
        if (bt.getDirection() != TransactionDirection.OUT || isCardSettlement(combinedDesc) || !hasCardPaymentKeyword(combinedDesc)) return false;
        return cards.stream().anyMatch(ca -> isMatch(bt, combinedDesc, ca));
    }

    private boolean isMatch(BankTransaction bt, String bankDesc, CardApproval ca) {
        if (bt.getOutAmount().compareTo(ca.getUsedAmount()) != 0) return false;
        if (Duration.between(bt.getTrDatetime(), ca.getUsedDatetime()).abs().toHours() > 6) return false;

        String normBankDesc = normalizeText(bankDesc);
        String normMerchant = normalizeText(ca.getMerchantName());

        if (normBankDesc.length() < 2 || normMerchant.length() < 2) return false;

        int lcsLength = getLongestCommonSubstringLength(normBankDesc, normMerchant);
        return lcsLength >= 4 || (double) lcsLength / Math.min(normBankDesc.length(), normMerchant.length()) >= 0.6;
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.replaceAll("[^a-zA-Z0-9가-힣]", "").replace("주식회사", "").replace("유한회사", "").replace("체크", "").replace("카드", "").toUpperCase();
    }

    private int getLongestCommonSubstringLength(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        int maxLength = 0;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxLength = Math.max(maxLength, dp[i][j]);
                } else {
                    dp[i][j] = 0;
                }
            }
        }
        return maxLength;
    }

    private boolean hasCardPaymentKeyword(String text) {
        return text != null && CARD_PAYMENT_KEYWORDS.stream().anyMatch(text::contains);
    }
}
