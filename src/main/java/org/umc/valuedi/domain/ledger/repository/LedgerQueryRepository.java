package org.umc.valuedi.domain.ledger.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.ledger.dto.response.CategoryStatResponse;
import org.umc.valuedi.domain.ledger.dto.response.DailyStatResponse;
import org.umc.valuedi.domain.ledger.dto.response.TrendResponse;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.enums.LedgerSortType;
import org.umc.valuedi.domain.ledger.enums.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import static org.umc.valuedi.domain.asset.entity.QBankTransaction.bankTransaction;
import static org.umc.valuedi.domain.asset.entity.QCardApproval.cardApproval;
import static org.umc.valuedi.domain.ledger.entity.QCategory.category;
import static org.umc.valuedi.domain.ledger.entity.QLedgerEntry.ledgerEntry;

@Repository
@RequiredArgsConstructor
public class LedgerQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<LedgerEntry> searchTransactions(Long memberId, YearMonth yearMonth, LocalDate date, Pageable pageable, LedgerSortType sort) {
        List<LedgerEntry> content = queryFactory
                .selectFrom(ledgerEntry)
                .leftJoin(ledgerEntry.category, category).fetchJoin()
                .leftJoin(ledgerEntry.bankTransaction, bankTransaction).fetchJoin()
                .leftJoin(ledgerEntry.cardApproval, cardApproval).fetchJoin()
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth),
                        dateEq(date)
                )
                .orderBy(getOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(ledgerEntry.count())
                .from(ledgerEntry)
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth),
                        dateEq(date)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<CategoryStatResponse> findCategoryStats(Long memberId, YearMonth yearMonth) {
        // 카드 금액 계산 (취소 상계)
        NumberExpression<Long> cardAmount = getCardAmountExpression();

        return queryFactory
                .select(Projections.constructor(CategoryStatResponse.class,
                        category.code,
                        category.name,
                        cardAmount.sumLong().coalesce(0L)
                )).from(ledgerEntry)
                .join(ledgerEntry.category, category)
                .leftJoin(ledgerEntry.cardApproval, cardApproval)
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth),
                        category.code.notIn("INCOME", "TRANSFER") // 지출만
                )
                .groupBy(category.code, category.name)
                .orderBy(cardAmount.sumLong().desc())
                .fetch();

    }

    // 3. 월 총 지출액 (비율 계산용)
    public Long findTotalExpense(Long memberId, YearMonth yearMonth) {
        NumberExpression<Long> cardAmount = getCardAmountExpression();

        return queryFactory
                .select(cardAmount.sumLong().coalesce(0L))
                .from(ledgerEntry)
                .leftJoin(ledgerEntry.cardApproval, cardApproval)
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth),
                        ledgerEntry.category.code.notIn("INCOME", "TRANSFER")
                )
                .fetchOne();
    }

    private BooleanExpression yearMonthEq(YearMonth yearMonth) {
        if (yearMonth == null) return null;
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        return ledgerEntry.transactionAt.between(start, end);
    }

    private BooleanExpression dateEq(LocalDate date) {
        if (date == null) return null;
        return ledgerEntry.transactionAt.between(date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    // 월 총 수입 조회
    public Long findTotalIncome(Long memberId, YearMonth yearMonth) {
        // 은행 입금만 수입으로 간주 (단순화)
        return queryFactory
                .select(bankTransaction.inAmount.sumLong().coalesce(0L))
                .from(ledgerEntry)
                .join(ledgerEntry.bankTransaction, bankTransaction)
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth),
                        ledgerEntry.transactionType.eq(TransactionType.INCOME)
                )
                .fetchOne();
    }

    // 일별 집계
    public List<DailyStatResponse> getDailyStats(Long memberId, YearMonth yearMonth) {
        // 일자별 그룹핑을 위해 String Template 사용 (DB 종속적일 수 있음, 여기선 MySQL 기준 DATE_FORMAT)
        // 또는 어플리케이션 레벨에서 그룹핑 가능. 여기선 어플리케이션 레벨 그룹핑 예시 (더 안전함)

        List<LedgerEntry> entries = queryFactory
                .selectFrom(ledgerEntry)
                .leftJoin(ledgerEntry.bankTransaction, bankTransaction).fetchJoin()
                .leftJoin(ledgerEntry.cardApproval, cardApproval).fetchJoin()
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        yearMonthEq(yearMonth)
                )
                .fetch();

        // Java Stream으로 집계
        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getTransactionAt().toLocalDate(),
                        Collectors.reducing(
                                new DailyStatResponse(null, 0L, 0L),
                                entry -> {
                                    long income = 0L;
                                    long expense = 0L;
                                    if (entry.getBankTransaction() != null) {
                                        income = entry.getBankTransaction().getInAmount();
                                        expense = entry.getBankTransaction().getOutAmount();
                                    } else if (entry.getCardApproval() != null) {
                                        // 카드 취소는 마이너스 지출로 처리
                                        if (entry.getCardApproval().getCancelYn().equals(CancelStatus.NORMAL)) { // NORMAL
                                            expense = entry.getCardApproval().getUsedAmount();
                                        } else {
                                            expense = -entry.getCardApproval().getUsedAmount();
                                        }
                                    }
                                    return new DailyStatResponse(entry.getTransactionAt().toLocalDate(), income, expense);
                                },
                                (a, b) -> new DailyStatResponse(
                                        a.getDate() != null ? a.getDate() : b.getDate(),
                                        a.getTotalIncome() + b.getTotalIncome(),
                                        a.getTotalExpense() + b.getTotalExpense()
                                )
                        )
                ))
                .values().stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    // 월별 추이
    public List<TrendResponse> findTrend(Long memberId, YearMonth from, YearMonth to) {
        // 기간 내의 모든 지출 내역 조회 후 Java에서 월별 그룹핑
        LocalDateTime start = from.atDay(1).atStartOfDay();
        LocalDateTime end = to.atEndOfMonth().atTime(LocalTime.MAX);

        List<LedgerEntry> entries = queryFactory
                .selectFrom(ledgerEntry)
                .leftJoin(ledgerEntry.cardApproval, cardApproval).fetchJoin()
                .leftJoin(ledgerEntry.bankTransaction, bankTransaction).fetchJoin()
                .where(
                        ledgerEntry.member.id.eq(memberId),
                        ledgerEntry.transactionAt.between(start, end),
                        ledgerEntry.category.code.notIn("INCOME", "TRANSFER") // 지출만
                )
                .fetch();

        return entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> YearMonth.from(entry.getTransactionAt()),
                        Collectors.summingLong(entry -> {
                            if (entry.getCardApproval() != null) {
                                // 카드 취소 고려
                                return entry.getCardApproval().getCancelYn().equals(CancelStatus.NORMAL)
                                        ? entry.getCardApproval().getUsedAmount()
                                        : -entry.getCardApproval().getUsedAmount();
                            } else if (entry.getBankTransaction() != null) {
                                return entry.getBankTransaction().getOutAmount();
                            }
                            return 0L;
                        })
                ))
                .entrySet().stream()
                .map(e -> new TrendResponse(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.getYearMonth().compareTo(b.getYearMonth()))
                .collect(Collectors.toList());
    }

    // 카드 금액 계산 로직 (승인 +, 취소 -)
    private NumberExpression<Long> getCardAmountExpression() {
        return new CaseBuilder()
                .when(ledgerEntry.transactionType.eq(TransactionType.EXPENSE)).then(cardApproval.usedAmount)
                .when(ledgerEntry.transactionType.eq(TransactionType.REFUND)).then(cardApproval.usedAmount.negate()) // REFUND는 차감
                .otherwise(0L);
    }

    private OrderSpecifier<?> getOrderSpecifier(LedgerSortType sort) {
        if (sort == null) return ledgerEntry.transactionAt.desc();

        switch (sort) {
            case LATEST:
                return ledgerEntry.transactionAt.desc();
            case OLDEST:
                return ledgerEntry.transactionAt.asc();
            // 금액 정렬은 복잡하므로(원천 테이블 조인 필요), 여기서는 transactionAt 기준으로 하거나
            // 별도의 NumberExpression을 만들어 정렬해야 함.
            // MVP에서는 날짜 정렬만 지원하거나, 금액 정렬 로직을 추가 구현해야 함.
            case AMOUNT_DESC:
                // TODO: 금액 정렬 구현 (복잡함)
                return ledgerEntry.transactionAt.desc();
            case AMOUNT_ASC:
                // TODO: 금액 정렬 구현
                return ledgerEntry.transactionAt.desc();
            default:
                return ledgerEntry.transactionAt.desc();
        }
    }
}


