package org.umc.valuedi.domain.connection.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;

public class ConnectionResDTO {




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
