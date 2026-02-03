package org.umc.valuedi.domain.goal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.util.List;
import java.util.Optional;

import static org.umc.valuedi.domain.asset.entity.QBankAccount.bankAccount;
import static org.umc.valuedi.domain.connection.entity.QCodefConnection.codefConnection;
import static org.umc.valuedi.domain.goal.entity.QGoal.goal;

@RequiredArgsConstructor
public class GoalRepositoryImpl implements GoalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Goal> findByIdWithDetails(Long goalId) {
        Goal result = queryFactory
                .selectFrom(goal)
                .leftJoin(goal.bankAccount, bankAccount).fetchJoin()
                .leftJoin(bankAccount.codefConnection, codefConnection).fetchJoin()
                .where(goal.id.eq(goalId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Goal> findAllByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, GoalStatus status) {
        return queryFactory
                .selectFrom(goal)
                .where(
                        goal.member.id.eq(memberId),
                        goal.status.eq(status)
                )
                .orderBy(goal.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Long> findLinkedBankAccountIdsByMemberId(Long memberId) {
        return queryFactory
                .select(goal.bankAccount.id)
                .from(goal)
                .where(
                        goal.member.id.eq(memberId),
                        goal.bankAccount.isNotNull()
                )
                .fetch();
    }

    @Override
    public Optional<Goal> findByIdAndMemberId(Long goalId, Long memberId) {
        Goal result = queryFactory
                .selectFrom(goal)
                .where(
                        goal.id.eq(goalId),
                        goal.member.id.eq(memberId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
