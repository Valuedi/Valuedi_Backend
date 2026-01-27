package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진행 중인 목표 개수 응답 DTO")
public record GoalActiveCountResponseDto(

        @Schema(description = "현재 진행 중인 목표 개수", example = "3")
        int count
) {}
