package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @NoArgsConstructor // Map 초기화를 위해 기본 생성자 추가
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

        @Builder.Default
        @Schema(description = "계좌 ID별 최신 잔액 맵")
        private Map<Long, Long> latestBalances = new HashMap<>();

        public boolean hasLatestBalanceFor(Long accountId) {
            return latestBalances != null && latestBalances.containsKey(accountId);
        }

        public Long getLatestBalanceFor(Long accountId) {
            return latestBalances.get(accountId);
        }

        public void addLatestBalance(Long accountId, Long balance) {
            this.latestBalances.put(accountId, balance);
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 거래/승인 내역")
    public static class AssetTransactionDetail {
        @Schema(description = "거래 일시", example = "2026-02-10T14:30:00")
        private LocalDateTime transactionAt;

        @Schema(description = "거래내역명", example = "초밥")
        private String title;

        @Schema(description = "금액", example = "16500")
        private Long amount;

        @Schema(description = "거래유형 (INCOME | EXPENSE)", example = "EXPENSE")
        private String transactionType;

        @Schema(description = "카테고리 코드", example = "FOOD")
        private String categoryCode;

        @Schema(description = "카테고리명", example = "식비")
        private String categoryName;

        @Schema(description = "거래 후 잔액 (카드는 null)", example = "93500")
        private Long afterBalance;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "자산 거래내역 페이지 응답")
    public static class AssetTransactionResponse {
        @Schema(description = "전체 항목 수", example = "42")
        private long totalElements;

        @Schema(description = "현재 페이지", example = "0")
        private int page;

        @Schema(description = "페이지 크기", example = "20")
        private int size;

        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;

        @Schema(description = "기관 코드", example = "0004")
        private String organizationCode;

        @Schema(description = "계좌명 또는 카드명", example = "KB나라사랑우대통장")
        private String assetName;

        @Schema(description = "카드번호 또는 계좌번호", example = "123-456-789012")
        private String assetNumber;

        @Schema(description = "현재 잔액 (카드는 null)", example = "150000")
        private Long currentBalance;

        @Schema(description = "거래내역 목록")
        private List<AssetTransactionDetail> content;
    }
}