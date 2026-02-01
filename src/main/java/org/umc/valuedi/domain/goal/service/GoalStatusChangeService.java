package org.umc.valuedi.domain.goal.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalStatusChangeService {

    private final GoalRepository goalRepository;
    private final GoalAchievementRateService achievementRateService; // 나중에 savedAmount 계산로직이 여기로

    // 1) 성공 처리: savedAmount >= targetAmount 인 ACTIVE 목표는 COMPLETE
    // 2) 실패 처리: endDate 도달/경과했고 savedAmount < targetAmount 인 ACTIVE 목표는 FAILED
    public void refreshGoalStatuses() {
        List<Goal> activeGoals = goalRepository.findAllByStatus(GoalStatus.ACTIVE);

        for (Goal goal : activeGoals) {
            int savedAmount = 0; // 계좌 연동 후 수정
            boolean isTargetReached = savedAmount >= goal.getTargetAmount();
            boolean isExpired = goal.getEndDate().isBefore(LocalDate.now());

            if (isTargetReached) {
                goal.Complete();
                continue;
            }

            if (isExpired) {
                goal.Fail();
            }
        }
    }
}
