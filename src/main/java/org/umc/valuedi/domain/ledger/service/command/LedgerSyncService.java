package org.umc.valuedi.domain.ledger.service.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.asset.enums.TransactionDirection;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
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
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Transactional
    public void syncTransactionsAndUpdateMember(Member member, LocalDate from, LocalDate to) {
        syncTransactions(member, from, to);
        member.updateLastSyncedAt();
    }

    @Transactional
    public void updateMemberLastSyncedAt(Member member) {
        member.updateLastSyncedAt();
    }

    @Transactional
    public void syncTransactions(Long memberId, LedgerSyncRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new LedgerException(MemberErrorCode.MEMBER_NOT_FOUND));
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();
        syncTransactions(member, from, to);
    }

    // 트랜잭션 어노테이션 제거 (상위 메서드에서 관리)
    public void syncTransactions(Member member, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new LedgerException(LedgerErrorCode.INVALID_DATE_RANGE);
        }

        Category defaultCategory = categoryRepository.findByCode("ETC")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));
        Category transferCategory = categoryRepository.findByCode("TRANSFER")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));

        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from.minusDays(1), to.plusDays(1));

        List<LedgerEntry> allNewEntries = new ArrayList<>();
        syncCardApprovals(member, from, to, defaultCategory, allNewEntries);
        syncBankTransactions(member, from, to, cards, defaultCategory, transferCategory, allNewEntries);

        if (!allNewEntries.isEmpty()) {
            ledgerEntryRepository.bulkInsert(allNewEntries);
        }
    }

    private void syncCardApprovals(Member member, LocalDate from, LocalDate to, Category defaultCategory, List<LedgerEntry> allNewEntries) {
        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from, to);

        for (CardApproval ca : cards) {
            if (ledgerEntryRepository.existsByCardApprovalId(ca.getId())) continue;
            if (ca.getUsedDatetime() == null) continue;

            String merchantName = ca.getMerchantName();
            Category category = mapCategoryByKeyword(ca.getMerchantType(), null);
            if (category == null) category = mapCategoryByKeyword(merchantName, defaultCategory);
            if (category == null) category = defaultCategory;

            TransactionType transactionType = ca.getCancelYn() == CancelStatus.NORMAL ? TransactionType.EXPENSE : TransactionType.INCOME;

            allNewEntries.add(LedgerEntry.builder()
                    .member(member).cardApproval(ca).category(category)
                    .title(ObjectUtils.isEmpty(merchantName) ? "카드 승인" : merchantName)
                    .transactionAt(ca.getUsedDatetime()).transactionType(transactionType).build());
        }
    }

    private void syncBankTransactions(Member member, LocalDate from, LocalDate to, List<CardApproval> cards, Category defaultCategory, Category transferCategory, List<LedgerEntry> allNewEntries) {
        List<BankTransaction> banks = bankTransactionRepository.findByTrDateBetween(from, to);
        for (BankTransaction bt : banks) {
            if (ledgerEntryRepository.existsByBankTransactionId(bt.getId())) continue;
            if (ObjectUtils.isEmpty(bt.getTrDatetime())) continue;

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

            allNewEntries.add(LedgerEntry.builder()
                    .member(member).bankTransaction(bt).category(category)
                    .title(title).transactionAt(bt.getTrDatetime()).transactionType(transactionType).build());
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
