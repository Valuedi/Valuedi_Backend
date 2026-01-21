package org.umc.valuedi.domain.goal.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalCommandService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    // 목표 생성
    public GoalCreateResponseDto createGoal(GoalCreateRequestDto req) {
        Member member = memberRepository.findById(req.memberId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateDateRange(req.startDate(), req.endDate());

        Goal goal = GoalConverter.toEntity(member, req);
        Goal saved = goalRepository.save(goal);

        return new GoalCreateResponseDto(saved.getId());
    }

    // 목표 수정
    public void updateGoal(Long goalId, GoalUpdateRequestDto req) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        // 취소 or 완료 된 목표는 수정 불가
        if (goal.getStatus() != GoalStatus.ACTIVE) throw new GoalException(GoalErrorCode.GOAL_NOT_EDITABLE);

        if (req.startDate() != null || req.endDate() != null) {
            validateDateRange(
                    req.startDate() != null ? req.startDate() : goal.getStartDate(),
                    req.endDate() != null ? req.endDate() : goal.getEndDate()
            );
        }

        GoalConverter.applyPatch(goal, req);
    }

    // 목표 삭제
    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        goalRepository.delete(goal);
    }

    // 날짜 검증
    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start.isAfter(end)) {
            throw new GoalException(GoalErrorCode.INVALID_DATE_RANGE);
        }
    }
}
