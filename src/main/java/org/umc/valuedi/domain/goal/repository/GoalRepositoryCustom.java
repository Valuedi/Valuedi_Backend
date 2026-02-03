package org.umc.valuedi.domain.goal.repository;

import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.util.List;
import java.util.Optional;

public interface GoalRepositoryCustom {
    Optional<Goal> findByIdWithDetails(Long goalId);

    List<Goal> findAllByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, GoalStatus status);

    List<Long> findLinkedBankAccountIdsByMemberId(Long memberId);

    Optional<Goal> findByIdAndMemberId(Long goalId, Long memberId);
}
