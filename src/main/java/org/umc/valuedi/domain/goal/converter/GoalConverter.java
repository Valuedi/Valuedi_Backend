package org.umc.valuedi.domain.goal.converter;

import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class GoalConverter {

    private GoalConverter() {}

    public static Goal toEntity(Member member, GoalCreateRequestDto req) {
        return Goal.builder()
                .member(member)
                .accountId(null)                 // 계좌 연동 되면 채우기
                .title(req.title())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .targetAmount(req.targetAmount())
                .status(GoalStatus.ACTIVE)
                .completedAt(null)
                .color(null)
                .icon(null)
                .build();
    }

    public static void applyPatch(Goal goal, GoalUpdateRequestDto req) {

        if (req.title() != null)
            goal.changeTitle(req.title());

        if (req.startDate() != null)
            goal.changeStartDate(req.startDate());

        if (req.endDate() != null)
            goal.changeEndDate(req.endDate());

        if (req.targetAmount() != null)
            goal.changeTargetAmount(req.targetAmount());
    }


    public static GoalListResponseDto.GoalSummaryDto toSummaryDto(
            Goal goal,
            Long savedAmount,
            int achievementRate
    ) {
        Long remainingAmount = Math.max(goal.getTargetAmount() - savedAmount, 0);
        Long remainingDays = calcRemainingDays(goal.getEndDate());

        return new GoalListResponseDto.GoalSummaryDto(
                goal.getId(),
                goal.getTitle(),
                remainingAmount,
                remainingDays,
                achievementRate,
                null, // 계좌 연동 되면 채우기
                goal.getStatus()
        );
    }

    public static GoalDetailResponseDto toDetailDto(
            Goal goal,
            Long savedAmount,
            int achievementRate
    ) {
        long remainingDays = calcRemainingDays(goal.getEndDate());

        return new GoalDetailResponseDto(
                goal.getId(),
                goal.getTitle(),
                savedAmount,
                goal.getTargetAmount(),
                remainingDays,
                achievementRate,
                null, // 계좌 연동 되면 채우기
                goal.getStatus()
        );
    }

    private static long calcRemainingDays(LocalDate endDate) {
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, endDate);
        return Math.max(days, 0);
    }
}
