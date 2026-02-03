package org.umc.valuedi.domain.goal.repository;

import org.umc.valuedi.domain.goal.entity.Goal;

import java.util.Optional;

public interface GoalRepositoryCustom {
    Optional<Goal> findByIdWithDetails(Long goalId);
}
