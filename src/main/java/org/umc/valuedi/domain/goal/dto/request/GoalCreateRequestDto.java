package org.umc.valuedi.domain.goal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalCreateRequestDto(
        @NotNull Long memberId,

        @NotNull
        @Size(max = 12)
        String title,

        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,

        @NotNull @Min(1) Integer targetAmount
) {}
