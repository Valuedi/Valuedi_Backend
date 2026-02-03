package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Primary 목표 목록 조회 응답 (ACTIVE 최신순, 간단 조회)")
public record GoalPrimaryListResponseDto(

        @Schema(description = "Primary 목표 목록")
        List<GoalPrimarySummaryDto> goals

) {
    @Schema(description = "Primary 목표 요약 정보")
    public record GoalPrimarySummaryDto(

            @Schema(description = "목표 ID", example = "10")
            Long goalId,

            @Schema(description = "목표 제목", example = "여행 자금 모으기")
            String title,

            @Schema(description = "목표 금액", example = "1000000")
            Long targetAmount,

            @Schema(description = "아이콘 ID", example = "3")
            Integer iconId

    ) {}
}
