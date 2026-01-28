package org.umc.valuedi.domain.asset.event;

import java.time.LocalDateTime;

public record AssetRawDataSavedEvent(
        Long connectionId,
        String sourceType,
        LocalDateTime timestamp
) {
    public AssetRawDataSavedEvent(Long connectionId, String sourceType) {
        this(connectionId, sourceType, LocalDateTime.now());
    }
}
