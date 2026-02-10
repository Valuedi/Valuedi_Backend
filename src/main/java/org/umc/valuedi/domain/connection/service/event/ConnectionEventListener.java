package org.umc.valuedi.domain.connection.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.umc.valuedi.domain.asset.service.command.AssetSyncService;
import org.umc.valuedi.domain.connection.dto.event.ConnectionSuccessEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionEventListener {

    private final AssetSyncService assetSyncService;

    @Async("assetFetchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConnectionSuccess(ConnectionSuccessEvent event) {
        try {
            assetSyncService.syncAssets(event.getConnection());
        } catch (Exception e) {
            log.error("[ConnectionEventListener] [handleConnectionSuccess] ERROR - 자산 동기화 중 오류 발생. Connection ID: {}", event.getConnection().getId(), e);
        }
    }
}
