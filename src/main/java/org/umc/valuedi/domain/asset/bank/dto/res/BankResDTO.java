package org.umc.valuedi.domain.asset.bank.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.asset.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;

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
}
