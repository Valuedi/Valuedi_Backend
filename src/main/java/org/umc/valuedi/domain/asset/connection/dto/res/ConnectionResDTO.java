package org.umc.valuedi.domain.asset.connection.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.umc.valuedi.domain.asset.connection.enums.BusinessType;
import org.umc.valuedi.domain.asset.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;

public class ConnectionResDTO {
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
    @Schema(description = "카드 연동 정보")
    public static class CardConnection {

        @Schema(description = "카드 ID", example = "card_123")
        private String cardId;

        @Schema(description = "카드명", example = "KB국민 나라사랑카드")
        private String cardName;

        @Schema(description = "카드번호 (마스킹)", example = "1234-****-****-5678")
        private String cardNum;

        @Schema(description = "카드사명", example = "KB카드")
        private String cardCompany;

        @Schema(description = "카드사 코드", example = "0301")
        private String cardCompanyCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연동 정보 (공통)")
    public static class Connection {

        @Schema(description = "연동 ID", example = "1")
        private Long id;

        @Schema(description = "기관 코드", example = "0020")
        private String organizationCode;

        @Schema(description = "기관명", example = "우리은행")
        private String organizationName;

        @Schema(description = "업무 구분", example = "BK")
        private BusinessType businessType;

        @Schema(description = "연동 일시", example = "2026-01-15T10:30:00")
        private LocalDateTime connectedAt;

        @Schema(description = "연동 상태", example = "ACTIVE")
        private ConnectionStatus status;
    }
}
