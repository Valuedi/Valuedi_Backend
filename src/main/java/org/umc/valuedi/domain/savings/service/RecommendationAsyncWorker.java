package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationAsyncWorker {

    private final RecommendationService recommendationService;
    private final RecommendationTxService recommendationTxService;

    @Async("recommendationExecutor")
    public void generateAndSaveAsync(Long memberId, Long batchId) {
        try {
            recommendationTxService.markProcessing(batchId);
            recommendationService.generateAndSaveRecommendations(memberId);
            recommendationTxService.markSuccess(batchId);
        } catch (Exception e) {
            log.error("[RecommendAsync] failed. memberId={}, batchId={}", memberId, batchId, e);
            recommendationTxService.markFailed(batchId, safeMsg(e));
        }
    }

    private String safeMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) return e.getClass().getSimpleName();
        return msg.length() > 450 ? msg.substring(0, 450) : msg;
    }
}

