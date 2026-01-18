package org.umc.valuedi.domain.asset.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
