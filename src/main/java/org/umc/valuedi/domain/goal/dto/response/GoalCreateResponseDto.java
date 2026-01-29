package org.umc.valuedi.domain.goal.dto.response;

import java.time.LocalDate;

public record GoalCreateResponseDto(
        Long goalId,
        String title,
        Long targetAmount,
        LocalDate startDate,

        LocalDate endDate,
        Long remainingDays,
        AccountDto account,
        Integer iconId
) {
    public record AccountDto(
            String bankName,
            String accountNumber
    ) {}
}
