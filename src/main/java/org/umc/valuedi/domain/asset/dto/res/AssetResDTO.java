package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    @Getter
    @AllArgsConstructor
    @Builder
    @Schema(description = "AssetFetchService의 내부 처리 결과 DTO (API 응답으로 직접 사용되지 않음)")
    public static class AssetSyncResult {
        @Schema(description = "새로 수집된 은행 거래내역 수")
        private int newBankTransactionCount;

        @Schema(description = "새로 수집된 카드 승인내역 수")
        private int newCardApprovalCount;

        @Schema(description = "데이터 수집에 성공한 기관 목록")
        private List<String> successOrganizations;

        @Schema(description = "데이터 수집에 실패한 기관 목록")
        private List<String> failureOrganizations;

        @Schema(description = "가계부 동기화에 사용될 조회 시작일")
        private LocalDate fromDate;

        @Schema(description = "가계부 동기화에 사용될 조회 종료일")
        private LocalDate toDate;
    }
}
