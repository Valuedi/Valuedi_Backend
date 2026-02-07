package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.genai.exception.GeminiException;
import org.umc.valuedi.global.external.genai.exception.code.GeminiErrorCode;

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
        } catch (GeminiException ge) {
            log.error("[RecommendAsync] failed. memberId={}, batchId={}", memberId, batchId, ge);

            String msg;
            GeminiErrorCode code = ge.getErrorCode();

            if (code == GeminiErrorCode.GEMINI_QUOTA_EXCEEDED) {
                msg = "Gemini 사용량/요청 제한이 초과되었습니다. 잠시 후(또는 내일) 다시 시도해 주세요.";
            } else if (code == GeminiErrorCode.GEMINI_TIMEOUT) {
                msg = "추천 생성 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
            } else {
                msg = "추천 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.";
            }

            recommendationTxService.markFailed(batchId, msg);
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

