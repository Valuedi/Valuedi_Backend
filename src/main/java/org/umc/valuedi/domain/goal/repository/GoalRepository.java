package org.umc.valuedi.domain.goal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByMember_Id( Long memberId);

    List<Goal> findAllByMember_IdAndStatus(Long memberId, GoalStatus status);
    List<Goal> findAllByMember_IdAndStatusIn(Long memberId, List<GoalStatus> statuses);
    long countByMember_IdAndStatus(Long memberId, GoalStatus status);

    List<Goal> findAllByStatus(GoalStatus status);
    List<Goal> findAllByStatusAndEndDateLessThanEqual(GoalStatus status, LocalDate date);
    List<Goal> findAllByMember_IdAndStatus(Long memberId, GoalStatus status, Pageable pageable);
    List<Goal> findAllByMember_IdAndStatusIn(Long memberId, List<GoalStatus> statuses, Pageable pageable);

}
