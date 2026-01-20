package org.umc.valuedi.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRateService achievementRateService;

    public GoalCreateResponseDto createGoal(GoalCreateRequestDto req) {
        Member member = memberRepository.findById(req.memberId())
                .orElseThrow(() -> new GoalException(GoalErrorCode.MEMBER_NOT_FOUND));

        validateDateRange(req.startDate(), req.endDate());

        Goal goal = GoalConverter.toEntity(member, req);
        Goal saved = goalRepository.save(goal);

        return new GoalCreateResponseDto(saved.getId());
    }

    @Transactional(readOnly = true)
    public GoalListResponseDto getGoals(Long memberId, GoalStatus status) {
        if (!memberRepository.existsById(memberId)) {
            throw new GoalException(GoalErrorCode.MEMBER_NOT_FOUND);
        }

        List<Goal> goals = goalRepository.findAllByMember_IdAndStatus(memberId, status);

        int savedAmount = 0; // 계좌 연동 후 수정!

        var dtos = goals.stream()
                .map(g -> GoalConverter.toSummaryDto(
                        g,
                        savedAmount,
                        achievementRateService.calculateRate(savedAmount, g.getTargetAmount())
                ))
                .toList();

        return new GoalListResponseDto(dtos);
    }


    @Transactional(readOnly = true)
    public GoalDetailResponseDto getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        int savedAmount = 0; // TODO
        int rate = achievementRateService.calculateRate(savedAmount, goal.getTargetAmount());

        return GoalConverter.toDetailDto(goal, savedAmount, rate);
    }

    public void updateGoal(Long goalId, GoalUpdateRequestDto req) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        // 날짜 수정 들어오면 검증
        if (req.startDate() != null || req.endDate() != null) {
            validateDateRange(
                    req.startDate() != null ? req.startDate() : goal.getStartDate(),
                    req.endDate() != null ? req.endDate() : goal.getEndDate()
            );
        }

        GoalConverter.applyPatch(goal, req);
    }

    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.changeStatus(GoalStatus.CANCELED);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start.isAfter(end)) {
            throw new GoalException(GoalErrorCode.INVALID_DATE_RANGE);
        }
    }
}
