package org.umc.valuedi.domain.connection.service;

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

    @Async("assetSyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConnectionSuccess(ConnectionSuccessEvent event) {
        log.info("금융사 연동 성공 이벤트 수신 - Connection ID: {}, Organization: {}",
                event.getConnection().getId(), event.getConnection().getOrganization());

        try {
            assetSyncService.syncAssets(event.getConnection());
        } catch (Exception e) {
            log.error("자산 동기화 중 오류 발생", e);
        }
    }
}
