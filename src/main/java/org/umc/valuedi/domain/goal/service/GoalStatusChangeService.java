package org.umc.valuedi.domain.goal.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoalStatusChangeService {

    private final GoalRepository goalRepository;

    // 전체 목표 상태 갱신 (스케줄러용)
    public void refreshGoalStatuses() {
        List<Goal> activeGoals = goalRepository.findAllByStatus(GoalStatus.ACTIVE);
        
        for (Goal goal : activeGoals) {
            try {
                BankAccount account = goal.getBankAccount();
                if (account == null || !account.getIsActive()) {
                    continue;
                }
                // DB에 저장된 계좌 잔액 사용
                Long currentBalance = account.getBalanceAmount();
                
                checkAndUpdateStatus(goal, currentBalance);
            } catch (Exception e) {
                log.error("목표 상태 갱신 중 오류 발생. Goal ID: {}", goal.getId(), e);
            }
        }
    }

    // 단일 목표 상태 갱신 (조회 시 호출용)
    public void checkAndUpdateStatus(Goal goal, long currentBalance) {
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            return;
        }

        boolean isTargetReached = currentBalance >= goal.getTargetAmount();
        // 목표 종료일이 오늘보다 이전이면 만료된 것으로 판단 (종료일 당일까지는 진행 중)
        boolean isExpired = goal.getEndDate().isBefore(LocalDate.now());

        if (isTargetReached) {
            goal.Complete();
        } else if (isExpired) {
            goal.Fail();
        }
    }
}
