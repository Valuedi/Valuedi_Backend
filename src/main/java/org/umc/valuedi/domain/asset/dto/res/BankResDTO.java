package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BankResDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "은행 연동 정보")
    public static class BankConnection {

        @Schema(description = "연동 ID", example = "1")
        private Long id;

        @Schema(description = "기관 코드", example = "0020")
        private String organizationCode;

        @Schema(description = "기관명", example = "우리은행")
        private String organizationName;

        @Schema(description = "연동 일시", example = "2026-01-15T10:30:00")
        private LocalDateTime connectedAt;

        @Schema(description = "연동 상태", example = "ACTIVE")
        private ConnectionStatus status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 계좌 정보")
    public static class BankAccountInfo {

        @Schema(description = "계좌명", example = "KB국민ONE통장")
        private String accountName;

        @Schema(description = "현재 잔액 (원 단위)", example = "450000")
        private Long balanceAmount;

        @Schema(description = "기관코드", example = "0020")
        private String organization;

        @Schema(description = "계좌 등록일시 (정렬 기준 확인용)", example = "2026-01-19T10:00:00")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "계좌 목록")
    public static class BankAccountListDTO {

        @Schema(description = "계좌 정보 목록")
        private List<BankAccountInfo> accountList;

        @Schema(description = "총 계좌 개수", example = "1")
        private Integer totalCount;
    }
}
