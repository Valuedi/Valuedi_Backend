package org.umc.valuedi.domain.goal.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalStatusCommandService {

    private final GoalRepository goalRepository;

    // 달성 완료
    public void completeGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new GoalException(GoalErrorCode.GOAL_STATUS_INVALID);
        }

        goal.Complete(); // 엔티티 메서드
    }

    // 달성 실패(취소 포함)
    public void failGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new GoalException(GoalErrorCode.GOAL_STATUS_INVALID);
        }

        goal.Fail();
    }

    // 다시 진행중으로 변경 (피그마에는 없음)
    public void activateGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.Activate();
    }
}
