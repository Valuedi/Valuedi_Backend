package org.umc.valuedi.domain.goal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByMember_Id( Long memberId);

    List<Goal> findAllByMember_IdAndStatus(Long memberId, GoalStatus status);
    long countByMember_IdAndStatus(Long memberId, GoalStatus status);
}
