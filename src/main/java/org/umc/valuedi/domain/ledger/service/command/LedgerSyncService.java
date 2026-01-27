package org.umc.valuedi.domain.ledger.service.command;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.TransactionDirection;
import org.umc.valuedi.domain.asset.repository.bank.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.CardApprovalRepository;
import org.umc.valuedi.domain.ledger.dto.request.LedgerSyncRequest;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.CategoryKeyword;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.exception.LedgerException;
import org.umc.valuedi.domain.ledger.exception.code.LedgerErrorCode;
import org.umc.valuedi.domain.ledger.repository.CategoryKeywordRepository;
import org.umc.valuedi.domain.ledger.repository.CategoryRepository;
import org.umc.valuedi.domain.ledger.repository.LedgerEntryRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
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
            "CARD", "DEBIT"
    );

    private static final List<String> CARD_SETTLEMENT_KEYWORDS = List.of(
            "카드대금", "신용카드대금", "카드청구", "카드자동이체"
    );

    // 메모리 캐시 (키워드 -> 카테고리)
    private Map<String, Category> keywordCache;

    @PostConstruct
    public void init() {
        refreshKeywordCache();
    }

    // 주기적으로 캐시 갱신이 필요하다면 별도 스케줄러 사용 가능
    public void refreshKeywordCache() {
        List<CategoryKeyword> keywords = categoryKeywordRepository.findAllWithCategory();
        keywordCache = keywords.stream()
                .collect(Collectors.toMap(CategoryKeyword::getKeyword, CategoryKeyword::getCategory, (existing, replacement) -> existing));
    }

    public void syncTransactions(Long memberId, LedgerSyncRequest request) {
        // 요청 파라미터 검증
        if (ObjectUtils.isEmpty(request.getYearMonth()) && ObjectUtils.isEmpty(request.getFromDate())) {
            throw new LedgerException(LedgerErrorCode.INVALID_SYNC_REQUEST);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new LedgerException(MemberErrorCode.MEMBER_NOT_FOUND));

        Category defaultCategory = categoryRepository.findByCode("ETC")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));
        Category transferCategory = categoryRepository.findByCode("TRANSFER")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));

        LocalDate from = ObjectUtils.isNotEmpty(request.getFromDate()) ? request.getFromDate() : request.getYearMonth().atDay(1);
        LocalDate to = ObjectUtils.isNotEmpty(request.getToDate()) ? request.getToDate() : request.getYearMonth().atEndOfMonth();

        syncCardApprovals(member, from, to, defaultCategory);
        syncBankTransactions(member, from, to, defaultCategory, transferCategory);
    }

    private void syncCardApprovals(Member member, LocalDate from, LocalDate to, Category defaultCategory) {
        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from, to);

        for (CardApproval ca : cards) {
            if (ledgerEntryRepository.existsByCardApprovalId(ca.getId())) continue;
            if (ObjectUtils.isEmpty(ca.getUsedDatetime())) continue;

            String merchantName = ca.getMerchantName();
            String merchantType = ca.getMerchantType();

            Category category = null;
            String transactionType;

            // 업종(merchantType)으로 먼저 매핑 시도
            if (!ObjectUtils.isEmpty(merchantType)) {
                category = mapCategoryByKeyword(merchantType, null);
            }

            // 업종 매핑 실패 시 가맹점명(merchantName)으로 매핑 시도
            if (ObjectUtils.isEmpty(category) && !ObjectUtils.isEmpty(merchantName)) {
                category = mapCategoryByKeyword(merchantName, defaultCategory);
            }

            // 모든 매핑 실패 시 기본 카테고리 사용
            if (ObjectUtils.isEmpty(category)) {
                category = defaultCategory;
            }

            transactionType = "출금";

            LedgerEntry entry = LedgerEntry.builder()
                    .member(member)
                    .cardApproval(ca)
                    .category(category)
                    .title(ObjectUtils.isEmpty(merchantName) ? "카드 승인" : merchantName)
                    .transactionAt(ca.getUsedDatetime())
                    .transactionType(transactionType) // transactionType 필드 설정
                    .build();
            ledgerEntryRepository.save(entry);
        }
    }

    public void syncBankTransactions(Member member, LocalDate from, LocalDate to, Category defaultCategory, Category transferCategory) {
        List<BankTransaction> banks = bankTransactionRepository.findByTrDateBetween(from, to);

        for (BankTransaction bt : banks) {
            // 이미 동기화된 내역은 스킵 (CardApproval에서 이미 처리했을 가능성 포함)
            if (ledgerEntryRepository.existsByBankTransactionId(bt.getId())) continue;

            // 필수 값 체크
            if (ObjectUtils.isEmpty(bt.getTrDatetime())) continue;

            // desc 필드 결합
            String combinedDesc = Stream.of(bt.getDesc2(), bt.getDesc3(), bt.getDesc4())
                    .filter(s -> !ObjectUtils.isEmpty(s))
                    .collect(Collectors.joining(" "));

            // 직불/체크카드 중복 결제 여부 판단
            if (isDebitCardDuplicate(combinedDesc)) continue;


            Category category;
            String transactionType;
            String title = ObjectUtils.isEmpty(combinedDesc) ? "은행 거래" : combinedDesc;
            if (title.length() > 50) {
                title = title.substring(0, 50);
            }

            if (isCardSettlement(combinedDesc)) {
                category = transferCategory;
                transactionType = "출금"; // 카드대금 정산은 출금
            } else {
                category = mapCategoryByKeyword(combinedDesc, defaultCategory);
                // BankTransaction의 direction을 따름
                if (bt.getDirection() == TransactionDirection.IN) {
                    transactionType = "입금";
                } else if (bt.getDirection() == TransactionDirection.OUT) {
                    transactionType = "출금";
                } else {
                    transactionType = "출금"; // 기본값
                }
            }

            LedgerEntry entry = LedgerEntry.builder()
                    .member(member)
                    .bankTransaction(bt)
                    .category(category)
                    .title(title)
                    .transactionAt(bt.getTrDatetime())
                    .transactionType(transactionType) // transactionType 필드 설정
                    .build();
            ledgerEntryRepository.save(entry);
        }
    }

    // --- 판단 함수 ---
    private boolean isCardSettlement(String text) {
        if (ObjectUtils.isEmpty(text)) return false;
        return CARD_SETTLEMENT_KEYWORDS.stream().anyMatch(text::contains);
    }

    private boolean isDebitCardDuplicate(String text) {
        if (ObjectUtils.isEmpty(text)) return false;

        // 정산/청구는 중복이 아니므로, 먼저 확인하여 제외
        if (isCardSettlement(text)) {
            return false;
        }

        return CARD_PAYMENT_KEYWORDS.stream().anyMatch(text::contains);
    }

    private Category mapCategoryByKeyword(String text, Category defaultCategory) {
        if (ObjectUtils.isEmpty(text) || text.isEmpty()) return defaultCategory;

        // 캐시된 키워드 맵을 순회하며 포함 여부 확인
        // (키워드가 많아지면 Aho-Corasick 등 알고리즘 최적화 필요, 현재는 단순 루프)
        for (Map.Entry<String, Category> entry : keywordCache.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultCategory;
    }

}
