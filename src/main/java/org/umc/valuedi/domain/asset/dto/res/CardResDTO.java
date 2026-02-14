package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class CardResDTO {
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
    @Schema(description = "카드사 연동 정보")
    public static class CardIssuerConnection {

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
    @Schema(description = "카드 정보")
    public static class CardInfo {
        @Schema(description = "카드 ID", example = "1")
        private Long cardId;

        @Schema(description = "카드명", example = "KB국민 나라사랑카드")
        private String cardName;

        @Schema(description = "카드번호 (마스킹)", example = "1234-****-****-5678")
        private String cardNoMasked;

        @Schema(description = "카드 타입", example = "CHECK")
        private String cardType;

        @Schema(description = "기관 코드", example = "0301")
        private String organization;

        @Schema(description = "생성 일시", example = "2024-05-20T10:00:00")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카드 목록 응답")
    public static class CardListDTO {
        @Schema(description = "카드 목록")
        private List<CardInfo> cardList;

        @Schema(description = "총 카드 수", example = "2")
        private Integer totalCount;
    }
}
