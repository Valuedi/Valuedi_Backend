package org.umc.valuedi.domain.goal.dto.response;

import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.util.List;

public record GoalListResponseDto(
        List<GoalSummaryDto> goals
) {
    public record GoalSummaryDto(
            Long goalId,
            String title,
            Long remainingAmount,
            Long remainingDays,
            Integer achievementRate,   // 0~100

            GoalStatus status,
            String colorCode,
            Integer iconId

    ) {}
}
