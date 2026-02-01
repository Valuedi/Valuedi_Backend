package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.response.GoalActiveCountResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalPrimaryListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.service.GoalAchievementRateService;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRateService achievementRateService;

    // 목표 상세 조회
    public GoalDetailResponseDto getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        BankAccount account = goal.getBankAccount();

        if (!account.getIsActive()) {
            throw new GoalException(GoalErrorCode.GOAL_ACCOUNT_INACTIVE);
        }

        long savedAmount = account.getBalanceAmount() - goal.getStartAmount(); // 계좌 연동 후 수정
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


    // 주요 목표 조회
    public GoalPrimaryListResponseDto getPrimaryGoals(Long memberId) {
        var goals = goalRepository.findAllByMember_IdAndStatusOrderByCreatedAtDesc(
                memberId, GoalStatus.ACTIVE
        );
        return GoalConverter.toPrimaryListResponse(goals);
    }
}
