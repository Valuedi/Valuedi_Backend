package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.client.CodefAuthClient;
import org.umc.valuedi.global.external.codef.config.CodefProperties;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefTokenService {

    private static final String CODEF_ACCESS_TOKEN_KEY = "codef_access_token";
    private static final long TOKEN_EXPIRATION_BUFFER_SECONDS = 300L;
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 3600L;

    private final CodefProperties codefProperties;
    private final CodefAuthClient codefAuthClient;
    private final StringRedisTemplate redisTemplate;

    /**
     * 유효한 Access Token 반환
     */
    public synchronized String getAccessToken() {
        // Redis에서 Access Token 조회
        String accessToken = redisTemplate.opsForValue().get(CODEF_ACCESS_TOKEN_KEY);

        // 토큰이 없으면 재발급 수행
        if (accessToken == null) {
            accessToken = fetchNewToken();
        }
        return accessToken;
    }

    /**
     * 토큰 재발급
     */
    private String fetchNewToken() {
        try {
            // Basic Auth 헤더 생성
            String auth = codefProperties.getClientId() + ":" + codefProperties.getClientSecret();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

            // 전용 FeignClient 호출
            Map<String, Object> response = codefAuthClient.getAccessToken(
                    basicAuth,
                    "client_credentials",
                    "read"
            );

            if (response != null && response.containsKey("access_token")) {
                log.info("CODEF Access Token 발급 성공");

                long expiresIn = DEFAULT_EXPIRES_IN_SECONDS; // 기본값 1시간
                if (response.containsKey("expires_in")) {
                    Object expiresInObj = response.get("expires_in");
                    if (expiresInObj instanceof Number) {
                        expiresIn = ((Number) expiresInObj).longValue();
                    } else if (expiresInObj instanceof String) {
                        expiresIn = Long.parseLong((String) expiresInObj);
                    }
                }

                String newAccessToken = (String) response.get("access_token");

                long ttl = Math.max(expiresIn - TOKEN_EXPIRATION_BUFFER_SECONDS, 60L);
                redisTemplate.opsForValue().set(CODEF_ACCESS_TOKEN_KEY, newAccessToken, ttl, TimeUnit.SECONDS);

                return newAccessToken;
            }
            throw new CodefException(CodefErrorCode.CODEF_TOKEN_ERROR);

        } catch (Exception e) {
            log.error("CODEF 토큰 발급 중 예외 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_TOKEN_ERROR);
        }
    }
}
