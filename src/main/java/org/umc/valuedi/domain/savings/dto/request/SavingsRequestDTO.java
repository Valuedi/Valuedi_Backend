package org.umc.valuedi.domain.savings.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class SavingsRequestDTO {

    public record RecommendRequest(
            @Schema(description = "회원 ID", example = "1")
            @NotNull Long memberId,

            @Schema(description = "적립 유형 필터 (S: 정액, F: 자유, null이면 전체)", example = "S", allowableValues = {"S", "F"})
            String rsrvType,

            @Schema(description = "선호 저축 기간(개월) - null이면 제한 없음", example = "12")
            Integer preferredSaveTerm
    ) {}
}
