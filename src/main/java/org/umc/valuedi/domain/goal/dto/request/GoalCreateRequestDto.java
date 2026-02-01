package org.umc.valuedi.domain.goal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "목표 추가")
public record GoalCreateRequestDto(

        @Schema(description = "회원 ID", example = "1")
        @NotNull Long memberId,

        @Schema(description = "목표 이름(최대 12자)", example = "유럽 여행 자금")
        @NotNull
        @NotBlank(message = "title은 공백일 수 없으며 필수입니다.")
        @Size(max = 12)
        String title,

        @Schema(description = "목표 시작일 (YYYY-MM-DD)", example = "2026-02-01")
        @NotNull LocalDate startDate,

        @Schema(description = "목표 종료일 (YYYY-MM-DD)", example = "2026-08-31")
        @NotNull LocalDate endDate,

        @Schema(description = "목표 금액(원 단위, 1 이상)", example = "3000000", minimum = "1")
        @NotNull(message = "targetAmount는 필수입니다.")
        @Min(value = 1, message = "targetAmount는 1 이상이어야 합니다.") Long targetAmount,

        @Schema(description = "색상 코드(HEX)", example = "FF6363")
        @NotBlank(message = "colorCode는 필수입니다.")
        String colorCode,

        @Schema(description = "아이콘 번호(1부터)", example = "1", minimum = "1")
        @NotNull(message = "iconId는 필수입니다.")
        Integer iconId

) {}
