package org.umc.valuedi.global.external.genai.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.external.genai.config.GeminiProperties;
import org.umc.valuedi.global.external.genai.exception.GeminiException;
import org.umc.valuedi.global.external.genai.exception.code.GeminiErrorCode;

import java.time.Duration;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final Client genaiClient;
    private final GeminiProperties geminiProperties;

    private static final int MAX_ATTEMPTS = 2;

    private static final Duration PER_ATTEMPT_TIMEOUT = Duration.ofSeconds(90);  // 시도별 제한
    private static final Duration OVERALL_DEADLINE = Duration.ofSeconds(190);  // 전체 제한

    private static final long BASE_BACKOFF_MILLIS = 2_000;
    private static final long MAX_BACKOFF_MILLIS = 8_000;

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r);
                t.setName("gemini-call");
                t.setDaemon(true);
                return t;
            });

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public String generateText(String prompt) {
        long start = System.nanoTime();
        Throwable lastCause = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            // 전체 데드라인 체크
            if (Duration.ofNanos(System.nanoTime() - start).compareTo(OVERALL_DEADLINE) > 0) {
                log.warn("Gemini overall deadline exceeded before attempt {} ({}s)", attempt, OVERALL_DEADLINE.toSeconds());
                throw new GeminiException(GeminiErrorCode.GEMINI_TIMEOUT);
            }

            try {
                String text = callWithTimeout(prompt, PER_ATTEMPT_TIMEOUT);

                if (text == null || text.isBlank()) {
                    throw new GeminiException(GeminiErrorCode.EMPTY_RESPONSE);
                }
                return text;
            } catch (GeminiException e) {
                Throwable cause = (e.getOriginalCause() != null) ? e.getOriginalCause() : e;

                if (isQuotaExceeded(cause)) {
                    throw new GeminiException(GeminiErrorCode.GEMINI_QUOTA_EXCEEDED, cause);
                }

                if (e.getErrorCode() == GeminiErrorCode.EMPTY_RESPONSE) {
                    throw e;
                }

                lastCause = e;

                if (!isRetryable(e)) {
                    throw e;
                }

                if (attempt == MAX_ATTEMPTS) {
                    throw new GeminiException(GeminiErrorCode.GEMINI_CALL_FAILED, e);
                }

                sleepBackoff(attempt, e);
            } catch (TimeoutException e) {
                lastCause = e;

                if (attempt == MAX_ATTEMPTS) {
                    log.warn("Gemini timeout exhausted after {} attempts", attempt, e);
                    throw new GeminiException(GeminiErrorCode.GEMINI_TIMEOUT, e);
                }

                sleepBackoff(attempt, e);
            } catch (Exception e) {
                // 할당량 초과는 재시도 금지
                if (isQuotaExceeded(e)) {
                    throw new GeminiException(GeminiErrorCode.GEMINI_QUOTA_EXCEEDED, e);
                }

                lastCause = e;

                if (attempt == MAX_ATTEMPTS || !isRetryable(e)) {
                    log.warn("Gemini timeout exhausted after {} attempts", attempt, e);
                    throw new GeminiException(GeminiErrorCode.GEMINI_CALL_FAILED, e);
                }
                sleepBackoff(attempt, e);
            }
        }
        throw new GeminiException(GeminiErrorCode.GEMINI_CALL_FAILED, lastCause);
    }

    private String callWithTimeout(String prompt, Duration timeout) throws TimeoutException, ExecutionException, InterruptedException {
        Future<String> future = executor.submit(() -> {
            GenerateContentResponse response = genaiClient.models.generateContent(geminiProperties.getModel(), prompt, null);
            return response.text();
        });

        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // 인터럽트 시도
            throw e;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private boolean isRetryable(Throwable t) {
        if (t == null) return false;

        if (t instanceof GeminiException ge && ge.getOriginalCause() != null) {
            t = ge.getOriginalCause();
        }

        if (isQuotaExceeded(t)) return false;

        Throwable root = rootCause(t);
        String msg = ((t.getMessage() == null ? "" : t.getMessage()) + " " + (root.getMessage() == null ? "" : root.getMessage())).toLowerCase();

        // 네트워크/연결 계열
        if (msg.contains("timeout") || msg.contains("timed out")) return true;
        if (msg.contains("connection reset") || msg.contains("broken pipe")) return true;

        // 502/503/504 등 HTTP 코드가 메시지에 포함되는 경우
        if (msg.contains("502") || msg.contains("bad gateway")) return true;
        if (msg.contains("503") || msg.contains("unavailable")) return true;
        if (msg.contains("504") || msg.contains("gateway timeout")) return true;

        return false;
    }

    private void sleepBackoff(int attempt, Throwable cause) {
        // attempt=1 실패 후 -> 2초, attempt=2 실패 후 -> 4초, attempt=3 -> 8초 ...
        long exp = 1L << (attempt - 1); // 1=>1, 2=>2, 3=>4 ...
        long backoff = BASE_BACKOFF_MILLIS * exp;

        // cap
        backoff = Math.min(backoff, MAX_BACKOFF_MILLIS);

        // jitter: 0~10%
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, backoff / 10));
        long sleepMillis = backoff + jitter;

        log.info("Gemini retry: attempt={} backoff={}ms (base={}ms cap={}ms) cause={}",
                attempt, sleepMillis, BASE_BACKOFF_MILLIS, MAX_BACKOFF_MILLIS, cause.toString());

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    private boolean isQuotaExceeded(Throwable t) {
        if (t == null) return false;

        String msg = String.valueOf(t.getMessage()).toLowerCase();
        if (msg.contains("resource_exhausted")
                || msg.contains("quota")
                || msg.contains("rate limit")
                || msg.contains("too many requests")
                || msg.contains("429")) {
            return true;
        }

        return isQuotaExceeded(t.getCause());
    }
}
