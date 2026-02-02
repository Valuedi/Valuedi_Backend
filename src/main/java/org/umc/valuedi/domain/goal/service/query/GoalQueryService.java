package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.response.GoalActiveCountResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.service.GoalAchievementRateService;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRateService achievementRateService;

    // 목표 상세 조회
    public GoalDetailResponseDto getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findByIdWithDetails(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        long savedAmount = 0; // 계좌 연동 후 수정
        if (goal.getBankAccount() != null && goal.getBankAccount().getBalanceAmount() != null) {
            savedAmount = goal.getBankAccount().getBalanceAmount();
        }

        int rate = achievementRateService.calculateRate(savedAmount, goal.getTargetAmount());

        return GoalConverter.toDetailDto(goal, savedAmount, rate);
    }

    // 목표 개수 조회
    public GoalActiveCountResponseDto getActiveGoalCount(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        long count = goalRepository.countByMember_IdAndStatus(memberId, GoalStatus.ACTIVE);
        return new GoalActiveCountResponseDto((int) count);
    }
}
