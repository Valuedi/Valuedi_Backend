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
            String bankName,            // 계좌 연동 되면 구현 할 예정입당

            GoalStatus status,
            String colorCode,
            Integer iconId

    ) {}
}
