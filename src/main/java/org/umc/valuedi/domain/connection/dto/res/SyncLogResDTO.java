package org.umc.valuedi.domain.connection.dto.res;

import lombok.*;

import java.time.LocalDateTime;

public class SyncLogResDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncLogResponseDTO {
        private Long syncLogId;
        private String syncStatus;
        private String syncType;
        private String errorMessage;
        private LocalDateTime updatedAt;
    }
}
