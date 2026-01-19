package org.umc.valuedi.domain.goal.dto.response;

import java.util.List;

public record GoalListResponseDto(
        List<GoalSummaryDto> goals
) {
    public record GoalSummaryDto(
            Long goalId,
            String title,
            Integer remainingAmount,
            Long remainingDays,
            Integer achievementRate,   // 0~100
            String bankName            // 계좌 연동 되면 구현 할 예정입당
    ) {}
}
