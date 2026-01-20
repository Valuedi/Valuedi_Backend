package org.umc.valuedi.domain.goal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "목표 추가")
public record GoalCreateRequestDto(

        @Schema(description = "회원 ID", example = "1")
        @NotNull Long memberId,

        @Schema(description = "목표 이름(최대 12자)", example = "유럽 여행 자금")
        @NotNull
        @Size(max = 12)
        String title,

        @Schema(description = "목표 시작일 (YYYY-MM-DD)", example = "2026-02-01")
        @NotNull LocalDate startDate,

        @Schema(description = "목표 종료일 (YYYY-MM-DD)", example = "2026-08-31")
        @NotNull LocalDate endDate,

        @Schema(description = "목표 금액(원 단위, 1 이상)", example = "3000000", minimum = "1")
        @NotNull @Min(1) Integer targetAmount

) {}
