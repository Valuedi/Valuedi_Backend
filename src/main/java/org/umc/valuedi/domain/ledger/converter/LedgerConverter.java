package org.umc.valuedi.domain.ledger.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.ledger.dto.response.CategoryStatResponse;
import org.umc.valuedi.domain.ledger.dto.response.DailyStatResponse;
import org.umc.valuedi.domain.ledger.dto.response.LedgerListResponse;
import org.umc.valuedi.domain.ledger.dto.response.LedgerSummaryResponse;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LedgerConverter {

    // --- Entity -> Response DTO ---

    public static LedgerListResponse.LedgerDetail toLedgerDetail(LedgerEntry entry) {
        // LedgerEntry의 transactionType 필드를 직접 사용
        String type = entry.getTransactionType(); // String 필드를 그대로 사용
        Long amount = 0L;

        // 금액은 여전히 원천 테이블에서 가져옴
        if (entry.getBankTransaction() != null) {
            BankTransaction bt = entry.getBankTransaction();
            if ("입금".equals(entry.getTransactionType())) { // transactionType이 "입금"이면 inAmount
                amount = bt.getInAmount();
            } else { // "출금"이면 outAmount
                amount = bt.getOutAmount();
            }
        } else if (entry.getCardApproval() != null) {
            CardApproval ca = entry.getCardApproval();
            if (ca.getCancelYn() == CancelStatus.NORMAL) { // 정상 승인
                amount = ca.getUsedAmount();
            } else if (ca.getCancelYn() == CancelStatus.CANCEL || ca.getCancelYn() == CancelStatus.PARTIAL_CANCEL) { // 취소 또는 부분 취소
                // 환불의 경우 금액을 음수로 표시하여 지출에서 상계되도록 처리
                amount = -ca.getUsedAmount();
            }
        }

        return LedgerListResponse.LedgerDetail.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .amount(amount)
                .type(type) // LedgerEntry의 transactionType 사용
                .categoryCode(entry.getCategory() != null ? entry.getCategory().getCode() : "ETC")
                .categoryName(entry.getCategory() != null ? entry.getCategory().getName() : "기타")
                .transactionAt(entry.getTransactionAt())
                .memo(entry.getMemo())
                .build();
    }

    public static LedgerListResponse toLedgerListResponse(List<LedgerEntry> entries, int page, int size, long totalElements) {
        List<LedgerListResponse.LedgerDetail> detailList = entries.stream()
                .map(LedgerConverter::toLedgerDetail)
                .collect(Collectors.toList());

        return LedgerListResponse.builder()
                .content(detailList)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .build();
    }

    public static LedgerSummaryResponse toLedgerSummaryResponse(Long totalIncome, Long totalExpense, Long prevMonthExpense) {
        return LedgerSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome - totalExpense)
                .diffFromLastMonth(totalExpense - (prevMonthExpense != null ? prevMonthExpense : 0L))
                .build();
    }

    public static CategoryStatResponse toCategoryStatResponse(String code, String name, Long amount, Long totalExpense) {
        double percentage = (totalExpense > 0) ? (double) amount / totalExpense * 100 : 0.0;
        return CategoryStatResponse.builder()
                .categoryCode(code)
                .categoryName(name)
                .totalAmount(amount)
                .percentage(Math.round(percentage * 10) / 10.0)
                .build();
    }

    public static DailyStatResponse toDailyStatResponse(LocalDate date, Long income, Long expense) {
        return DailyStatResponse.builder()
                .date(date)
                .totalIncome(income)
                .totalExpense(expense)
                .build();
    }

    // --- source -> Entity (Sync용) ---
    public static LedgerEntry toLedgerEntry(Member member, BankTransaction bankTransaction, Category category) {
        return LedgerEntry.builder()
                .member(member)
                .bankTransaction(bankTransaction)
                .category(category)
                .title(bankTransaction.getDesc3()) // 적요를 제목으로
                .transactionAt(bankTransaction.getTrDatetime())
                .isUserModified(false)
                .build();
    }

    public static LedgerEntry toLedgerEntry(Member member, CardApproval cardApproval, Category category) {
        return LedgerEntry.builder()
                .member(member)
                .cardApproval(cardApproval)
                .category(category)
                .title(cardApproval.getMerchantName()) // 가맹점명을 제목으로
                .transactionAt(cardApproval.getUsedDatetime())
                .isUserModified(false)
                .build();
    }
}
