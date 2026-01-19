package org.umc.valuedi.domain.goal.dto.response;

public record GoalDetailResponseDto(
        Long goalId,
        String title,
        Integer savedAmount,
        Integer targetAmount,
        Long remainingDays,
        Integer achievementRate, // 0~100
        AccountDto account
) {
    public record AccountDto(
            String bankName,
            String accountNumber
    ) {}
}
