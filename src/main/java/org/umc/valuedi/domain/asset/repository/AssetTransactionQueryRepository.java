package org.umc.valuedi.domain.asset.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.umc.valuedi.domain.asset.entity.QBankAccount.bankAccount;
import static org.umc.valuedi.domain.asset.entity.QBankTransaction.bankTransaction;
import static org.umc.valuedi.domain.asset.entity.QCard.card;
import static org.umc.valuedi.domain.asset.entity.QCardApproval.cardApproval;
import static org.umc.valuedi.domain.connection.entity.QCodefConnection.codefConnection;
import static org.umc.valuedi.domain.ledger.entity.QCategory.category;
import static org.umc.valuedi.domain.ledger.entity.QLedgerEntry.ledgerEntry;

@Repository
@RequiredArgsConstructor
public class AssetTransactionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<BankAccount> findAccountWithConnection(Long accountId, Long memberId) {
        BankAccount result = queryFactory
                .selectFrom(bankAccount)
                .join(bankAccount.codefConnection, codefConnection).fetchJoin()
                .where(
                        bankAccount.id.eq(accountId),
                        codefConnection.member.id.eq(memberId)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public Optional<Card> findCardWithConnection(Long cardId, Long memberId) {
        Card result = queryFactory
                .selectFrom(card)
                .join(card.codefConnection, codefConnection).fetchJoin()
                .where(
                        card.id.eq(cardId),
                        codefConnection.member.id.eq(memberId)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public Page<BankTransaction> findBankTransactions(
            Long accountId, YearMonth yearMonth, LocalDate date, Pageable pageable) {

        List<BankTransaction> content = queryFactory
                .selectFrom(bankTransaction)
                .where(
                        bankTransaction.bankAccount.id.eq(accountId),
                        bankYearMonthEq(yearMonth),
                        bankDateEq(date)
                )
                .orderBy(bankTransaction.trDatetime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(bankTransaction.count())
                .from(bankTransaction)
                .where(
                        bankTransaction.bankAccount.id.eq(accountId),
                        bankYearMonthEq(yearMonth),
                        bankDateEq(date)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public Page<CardApproval> findCardApprovals(
            Long cardId, YearMonth yearMonth, LocalDate date, Pageable pageable) {

        List<CardApproval> content = queryFactory
                .selectFrom(cardApproval)
                .where(
                        cardApproval.card.id.eq(cardId),
                        cardYearMonthEq(yearMonth),
                        cardDateEq(date)
                )
                .orderBy(cardApproval.usedDatetime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(cardApproval.count())
                .from(cardApproval)
                .where(
                        cardApproval.card.id.eq(cardId),
                        cardYearMonthEq(yearMonth),
                        cardDateEq(date)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<LedgerEntry> findLedgerEntriesForBankTransactions(List<Long> bankTransactionIds) {
        if (bankTransactionIds.isEmpty()) return List.of();
        return queryFactory
                .selectFrom(ledgerEntry)
                .join(ledgerEntry.category, category).fetchJoin()
                .where(ledgerEntry.bankTransaction.id.in(bankTransactionIds))
                .fetch();
    }

    public List<LedgerEntry> findLedgerEntriesForCardApprovals(List<Long> cardApprovalIds) {
        if (cardApprovalIds.isEmpty()) return List.of();
        return queryFactory
                .selectFrom(ledgerEntry)
                .join(ledgerEntry.category, category).fetchJoin()
                .where(ledgerEntry.cardApproval.id.in(cardApprovalIds))
                .fetch();
    }

    private BooleanExpression bankYearMonthEq(YearMonth ym) {
        if (ym == null) return null;
        return bankTransaction.trDatetime.between(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(LocalTime.MAX)
        );
    }

    private BooleanExpression bankDateEq(LocalDate date) {
        if (date == null) return null;
        return bankTransaction.trDatetime.between(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );
    }

    private BooleanExpression cardYearMonthEq(YearMonth ym) {
        if (ym == null) return null;
        return cardApproval.usedDatetime.between(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(LocalTime.MAX)
        );
    }

    private BooleanExpression cardDateEq(LocalDate date) {
        if (date == null) return null;
        return cardApproval.usedDatetime.between(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );
    }
}
