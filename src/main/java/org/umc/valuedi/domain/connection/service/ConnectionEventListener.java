package org.umc.valuedi.domain.connection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.service.AssetSyncService;
import org.umc.valuedi.domain.connection.dto.event.ConnectionSuccessEvent;
import org.umc.valuedi.global.external.codef.exception.CodefException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionEventListener {

    private final AssetSyncService assetSyncService;

    @Async("assetSyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConnectionSuccess(ConnectionSuccessEvent event) {
        Long connectionId = event.getConnection().getId();
        String organization = event.getConnection().getOrganization();

        log.info("금융사 연동 성공 이벤트 수신, 자산 동기화 시작 - Connection ID: {}, Organization: {}", connectionId, organization);

        try {
            assetSyncService.syncAssets(event.getConnection());
            log.info("자산 동기화 성공 - Connection ID: {}", connectionId);
        } catch (AssetException e) {
            log.error("자산 동기화 실패 - Connection ID: {}, ErrorCode: {}, Message: {}", connectionId, e.getCode(), e.getMessage());
        } catch (CodefException e) {
            log.error("자산 동기화 중 CODEF API 오류 발생 - Connection ID: {}, ErrorCode: {}, Message: {}", connectionId, e.getCode(), e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 모든 예외 처리
            log.error("자산 동기화 중 예상치 못한 오류 발생 - Connection ID: {}", connectionId, e);
        }
    }
}
