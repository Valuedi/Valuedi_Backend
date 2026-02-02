package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.asset.entity.BankAccount;

import java.util.List;
import java.util.Optional;

import static org.umc.valuedi.domain.asset.entity.QBankAccount.bankAccount;
import static org.umc.valuedi.domain.connection.entity.QCodefConnection.codefConnection;
import static org.umc.valuedi.domain.goal.entity.QGoal.goal;

@RequiredArgsConstructor
public class BankAccountRepositoryImpl implements BankAccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BankAccount> findAllByMemberIdAndOrganization(Long memberId, String organization) {
        return queryFactory
                .selectFrom(bankAccount)
                .join(bankAccount.codefConnection, codefConnection).fetchJoin()
                .leftJoin(bankAccount.goal, goal).fetchJoin()
                .where(
                        codefConnection.member.id.eq(memberId),
                        codefConnection.organization.eq(organization),
                        bankAccount.isActive.isTrue()
                )
                .fetch();
    }

    @Override
    public List<BankAccount> findAllByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(bankAccount)
                .join(bankAccount.codefConnection, codefConnection).fetchJoin()
                .leftJoin(bankAccount.goal, goal).fetchJoin()
                .where(
                        codefConnection.member.id.eq(memberId),
                        bankAccount.isActive.isTrue()
                )
                .orderBy(bankAccount.createdAt.desc())
                .fetch();
    }

    @Override
    public long countByMemberId(Long memberId) {
        Long count = queryFactory
                .select(bankAccount.count())
                .from(bankAccount)
                .join(bankAccount.codefConnection, codefConnection)
                .where(
                        codefConnection.member.id.eq(memberId),
                        bankAccount.isActive.isTrue()
                )
                .fetchOne();
        return count != null ? count : 0;
    }

    @Override
    public List<BankAccount> findUnlinkedByMemberId(Long memberId, List<Long> excludeIds) {
        // excludeIds가 null/empty면 NOT IN 조건을 빼는 방식이 제일 안전함
        boolean hasExcludeIds = (excludeIds != null && !excludeIds.isEmpty());

        var base = queryFactory
                .selectFrom(bankAccount)
                .join(bankAccount.codefConnection, codefConnection).fetchJoin()
                .leftJoin(bankAccount.goal, goal).fetchJoin()
                .where(
                        codefConnection.member.id.eq(memberId),
                        bankAccount.isActive.isTrue()
                );

        if (hasExcludeIds) {
            base.where(bankAccount.id.notIn(excludeIds));
        }

        return base
                .orderBy(bankAccount.id.desc())
                .fetch();
    }

    @Override
    public Optional<BankAccount> findByIdAndMemberId(Long accountId, Long memberId) {
        BankAccount result = queryFactory
                .selectFrom(bankAccount)
                .join(bankAccount.codefConnection, codefConnection).fetchJoin()
                .leftJoin(bankAccount.goal, goal).fetchJoin()
                .where(
                        bankAccount.id.eq(accountId),
                        codefConnection.member.id.eq(memberId),
                        bankAccount.isActive.isTrue()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
