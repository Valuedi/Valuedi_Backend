package org.umc.valuedi.domain.goal.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long>, GoalRepositoryCustom {

    List<Goal> findAllByMember_Id( Long memberId);

    List<Goal> findAllByMember_IdAndStatus(Long memberId, GoalStatus status);
    List<Goal> findAllByMember_IdAndStatusIn(Long memberId, List<GoalStatus> statuses);
    long countByMember_IdAndStatus(Long memberId, GoalStatus status);

    List<Goal> findAllByStatus(GoalStatus status);
    List<Goal> findAllByStatusAndEndDateLessThanEqual(GoalStatus status, LocalDate date);
    List<Goal> findAllByMember_IdAndStatus(Long memberId, GoalStatus status, Pageable pageable);
    List<Goal> findAllByMember_IdAndStatusIn(Long memberId, List<GoalStatus> statuses, Pageable pageable);


    List<Goal> findAllByMember_IdAndStatusOrderByCreatedAtDesc(Long memberId, GoalStatus status);
    @Query("""
        SELECT g.bankAccount.id
        FROM Goal g
        WHERE g.member.id = :memberId
          AND g.bankAccount IS NOT NULL
    """)
    List<Long> findLinkedBankAccountIdsByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT g
        FROM Goal g
        WHERE g.id = :goalId
          AND g.member.id = :memberId
    """)
    Optional<Goal> findByIdAndMemberId(@Param("goalId") Long goalId, @Param("memberId") Long memberId);

    boolean existsByBankAccount_Id(Long bankAccountId);

}
