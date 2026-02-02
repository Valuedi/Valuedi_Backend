package org.umc.valuedi.domain.goal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.goal.entity.Goal;

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
}
