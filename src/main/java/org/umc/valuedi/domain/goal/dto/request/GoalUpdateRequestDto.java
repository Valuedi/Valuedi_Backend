package org.umc.valuedi.domain.goal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalUpdateRequestDto(
        @Size(max = 12)
        String title,

        LocalDate startDate,
        LocalDate endDate,

        @Min(1)
        Integer targetAmount
) {}
