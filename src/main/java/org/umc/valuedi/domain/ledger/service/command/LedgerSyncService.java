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
import java.util.Collections;
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

    // 주기적으로 캐시 갱신이 필요하다면 별도 스케줄러 사용 가능
    public void refreshKeywordCache() {
        List<CategoryKeyword> keywords = categoryKeywordRepository.findAllWithCategory();
        Map<String, Category> newCache = keywords.stream()
                .collect(Collectors.toMap(CategoryKeyword::getKeyword, CategoryKeyword::getCategory, (existing, replacement) -> existing));
        this.keywordCache = Collections.unmodifiableMap(newCache);
    }

    public void syncTransactions(Long memberId, LedgerSyncRequest request) {
        // 요청 파라미터 검증
        if (ObjectUtils.isEmpty(request.getYearMonth()) && ObjectUtils.isEmpty(request.getFromDate()) || ObjectUtils.isEmpty(request.getToDate())) {
            throw new LedgerException(LedgerErrorCode.INVALID_SYNC_REQUEST);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new LedgerException(MemberErrorCode.MEMBER_NOT_FOUND));

        Category defaultCategory = categoryRepository.findByCode("ETC")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));
        Category transferCategory = categoryRepository.findByCode("TRANSFER")
                .orElseThrow(() -> new LedgerException(LedgerErrorCode.CATEGORY_NOT_FOUND));

        // 2. 날짜 범위 설정 로직 개선
        LocalDate from;
        LocalDate to;

        if (ObjectUtils.isNotEmpty(request.getYearMonth())) {
            // Case 1: yearMonth가 있는 경우 -> 해당 월 전체 (1일 ~ 말일)
            from = request.getYearMonth().atDay(1);
            to = request.getYearMonth().atEndOfMonth();
        } else {
            // Case 2: fromDate가 있는 경우
            from = request.getFromDate();
            if (ObjectUtils.isNotEmpty(request.getToDate())) {
                // Case 2-1: toDate도 있는 경우 -> fromDate ~ toDate
                to = request.getToDate();
            } else {
                // Case 2-2: toDate가 없는 경우 -> fromDate ~ 오늘(현재)
                to = LocalDate.now();
            }
        }

        // 유효성 검사: 종료일이 시작일보다 앞서면 안됨
        if (to.isBefore(from)) {
            throw new LedgerException(LedgerErrorCode.INVALID_DATE_RANGE); // 에러 코드 추가 필요
        }

        // 카드 승인 내역 미리 조회 (매칭 오차 고려 +-1일 버퍼)
        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from.minusDays(1), to.plusDays(1));

        // 카드 승인 내역 동기화 (조회된 리스트 전달)
        syncCardApprovals(member, from, to, defaultCategory);

        // 은행 거래 내역 동기화 (조회된 카드 리스트 전달하여 중복 매칭)
        syncBankTransactions(member, from, to, cards, defaultCategory, transferCategory);
    }

    private void syncCardApprovals(Member member, LocalDate from, LocalDate to, Category defaultCategory) {
        List<CardApproval> cards = cardApprovalRepository.findByUsedDateBetween(from, to);

        for (CardApproval ca : cards) {
            if (ledgerEntryRepository.existsByCardApprovalId(ca.getId())) continue;
            if (ObjectUtils.isEmpty(ca.getUsedDatetime())) continue;

            String merchantName = ca.getMerchantName();
            String merchantType = ca.getMerchantType();

            Category category = null;
            TransactionType transactionType;

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

            if (ca.getCancelYn().equals(CancelStatus.NORMAL)) {
                transactionType = TransactionType.EXPENSE;
            } else {
                transactionType = TransactionType.INCOME;
            }

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

    public void syncBankTransactions(Member member, LocalDate from, LocalDate to, List<CardApproval> cards, Category defaultCategory, Category transferCategory) {
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

            // 중복 제거 로직 변경 : 키워드 포함 시 + 실제 매칭 성공 시에만 스킵
            if (isDuplicateOfCardApproval(bt, combinedDesc, cards)) continue;


            Category category;
            TransactionType transactionType;
            String title = ObjectUtils.isEmpty(combinedDesc) ? "은행 거래" : combinedDesc;
            if (title.length() > 50) {
                title = title.substring(0, 50);
            }

            if (isCardSettlement(combinedDesc)) {
                category = transferCategory;
                transactionType = TransactionType.EXPENSE; // 카드대금 정산은 출금
            } else {
                category = mapCategoryByKeyword(combinedDesc, defaultCategory);
                // BankTransaction의 direction을 따름
                if (bt.getDirection() == TransactionDirection.IN) {
                    transactionType = TransactionType.INCOME;
                } else if (bt.getDirection() == TransactionDirection.OUT) {
                    transactionType = TransactionType.EXPENSE;
                } else {
                    transactionType = TransactionType.EXPENSE; // 기본값
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

    // 은행 거래가 카드 승인 내역과 중복되는지 정밀하게 판단
    private boolean isDuplicateOfCardApproval(BankTransaction bt, String combinedDesc, List<CardApproval> cards) {
        // 1. 출금(OUT)인 경우에만 체크
        if (bt.getDirection() != TransactionDirection.OUT) return false;

        // 2. 정산/청구 키워드가 있으면 중복 아님 (TRANSFER로 처리됨)
        if (isCardSettlement(combinedDesc)) return false;

        // 3. 카드 결제 후보 키워드가 있는 경우에만 매칭 시도 (선택 사항이지만 성능 최적화 위해 적용)
        if (!hasCardPaymentKeyword(combinedDesc)) return false;

        // 4. 실제 매칭 로직 (금액, 시간, 상호명 유사도)
        return cards.stream().anyMatch(ca -> isMatch(bt, combinedDesc, ca));
    }

    private boolean isMatch(BankTransaction bt, String bankDesc, CardApproval ca) {
        // 1. 금액 일치 여부 (정확히 일치)
        if (bt.getOutAmount().compareTo(ca.getUsedAmount()) != 0) return false;

        // 2. 시간 근접 여부 (±6시간 이내)
        long hoursDiff = Duration.between(bt.getTrDatetime(), ca.getUsedDatetime()).abs().toHours();
        if (hoursDiff > 6) return false;

        // 3. 상호/적요 유사도 (정규화 후 포함 여부 확인)
        String normBankDesc = normalizeText(bankDesc);
        String normMerchant = normalizeText(ca.getMerchantName());

        // 한쪽이 비어있으면 매칭 불가
        if (normBankDesc.isEmpty() || normMerchant.isEmpty()) return false;

        // 부분 포함 여부로 판단 (더 정교한 알고리즘 적용 가능)
        return normBankDesc.contains(normMerchant) || normMerchant.contains(normBankDesc);
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-Z0-9가-힣]", "") // 특수문자, 공백 제거
                .replace("주식회사", "")
                .replace("주", "")
                .replace("유한회사", "")
                .toUpperCase();
    }


    private boolean hasCardPaymentKeyword(String text) {
        if (ObjectUtils.isEmpty(text)) return false;
        return CARD_PAYMENT_KEYWORDS.stream().anyMatch(text::contains);
    }


}
