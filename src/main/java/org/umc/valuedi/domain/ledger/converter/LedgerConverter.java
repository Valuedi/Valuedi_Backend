package org.umc.valuedi.domain.ledger.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.ledger.dto.response.DailyStatResponse;
import org.umc.valuedi.domain.ledger.dto.response.LedgerListResponse;
import org.umc.valuedi.domain.ledger.dto.response.LedgerSummaryResponse;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.util.List;

@Component
public class LedgerConverter {

    // --- Entity -> Response DTO ---

    public static LedgerListResponse.LedgerDetail toLedgerDetail(LedgerEntry entry) {
        String type = "EXPENSE";
        Long amount = 0L;

        if (entry.getBankTransaction() != null) {
            // 은행 거래 : 입금이면 INCOME, 출금이면 EXPENSE
            if (entry.getBankTransaction().getInAmount() > 0) {
                type = "INCOME";
                amount = entry.getBankTransaction().getInAmount();
            } else {
                amount = entry.getBankTransaction().getOutAmount();
            }
        } else if (entry.getCardApproval() != null) {
            // 카드거래 : 승인 금액 사용
            amount = entry.getCardApproval().getUsedAmount();
        }

        return LedgerListResponse.LedgerDetail.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .amount(amount)
                .type(type)
                .categoryCode(entry.getCategory() != null ? entry.getCategory().getCode() : "ETC")
                .categoryName(entry.getCategory() != null ? entry.getCategory().getName() : "기타")
                .transactionAt(entry.getTransactionAt())
                .memo(entry.getMemo())
                .build();
    }

    public static LedgerListResponse toLedgerListResponse(List<LedgerEntry> entries, int page, int size, long totalElements) {
        List<LedgerListResponse.LedgerDetail> detailList = entries.stream()
                .map(LedgerConverter::toLedgerDetail)
                .toList();

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
                .title(bankTransaction.getDesc3())
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
