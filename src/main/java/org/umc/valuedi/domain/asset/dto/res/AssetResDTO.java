package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "자산 통합 응답 DTO")
public class AssetResDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연동 자산 총 개수 응답")
    public static class AssetSummaryCountDTO {
        @Schema(description = "연동된 총 계좌 수", example = "5")
        private Long totalAccountCount;

        @Schema(description = "연동된 총 카드 수", example = "3")
        private Long totalCardCount;

        @Schema(description = "총 연동 자산 수 (계좌 + 카드)", example = "8")
        private Long totalAssetCount;
    }
}