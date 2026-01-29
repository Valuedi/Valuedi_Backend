package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.util.List;

@Schema(description = "목표 목록 조회 응답")
public record GoalListResponseDto(

        @Schema(description = "목표 요약 목록")
        List<GoalSummaryDto> goals

) {
    @Schema(description = "목표 요약 정보")
    public record GoalSummaryDto(

            @Schema(description = "목표 ID", example = "10")
            Long goalId,

            @Schema(description = "목표 제목", example = "여행 자금 모으기")
            String title,

            @Schema(description = "남은 금액", example = "700000")
            Long remainingAmount,

            @Schema(description = "남은 일수", example = "30")
            Long remainingDays,

            @Schema(description = "달성률(0~100)", example = "30")
            Integer achievementRate,

            @Schema(description = "목표 상태", example = "ACTIVE")
            GoalStatus status,

            @Schema(description = "색상 코드(# 제외)", example = "FF6363")
            String colorCode,

            @Schema(description = "아이콘 ID", example = "3")
            Integer iconId

    ) {}
}
