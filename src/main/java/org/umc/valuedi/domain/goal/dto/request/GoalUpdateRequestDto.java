package org.umc.valuedi.domain.goal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "목표 수정")
public record GoalUpdateRequestDto(

        @Schema(description = "목표 이름(최대 12자)", example = "유럽 여행 자금")
        @Size(max = 12)
        String title,

        @Schema(description = "목표 시작일 (YYYY-MM-DD)", example = "2026-02-01")
        LocalDate startDate,

        @Schema(description = "목표 종료일 (YYYY-MM-DD)", example = "2026-08-31")
        LocalDate endDate,

        @Schema(description = "목표 금액(원 단위, 1 이상)", example = "3500000", minimum = "1")
        @Min(1)
        Integer targetAmount

) {}
