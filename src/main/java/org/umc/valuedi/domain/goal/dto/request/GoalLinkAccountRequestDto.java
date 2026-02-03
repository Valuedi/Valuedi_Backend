package org.umc.valuedi.domain.goal.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "목표-계좌 연결 요청")
public record GoalLinkAccountRequestDto(

        @Schema(description = "연결할 계좌 ID", example = "12")
        @NotNull(message = "accountId는 필수입니다.")
        Long accountId
) {}
