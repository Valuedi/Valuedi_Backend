package org.umc.valuedi.domain.savings.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class SavingsRequestDTO {

    public record RecommendRequest(
            @Schema(description = "회원 ID", example = "1")
            @NotNull Long memberId
    ) {}
}
