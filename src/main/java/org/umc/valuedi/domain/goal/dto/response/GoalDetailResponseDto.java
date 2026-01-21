package org.umc.valuedi.domain.goal.dto.response;

import org.umc.valuedi.domain.goal.enums.GoalStatus;

public record GoalDetailResponseDto(
        Long goalId,
        String title,
        Long savedAmount,
        Long targetAmount,
        Long remainingDays,
        Integer achievementRate, // 0~100
        AccountDto account,
        GoalStatus status
) {
    public record AccountDto(
            String bankName,
            String accountNumber
    ) {}
}
