package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.client.CodefAuthClient;
import org.umc.valuedi.global.external.codef.config.CodefProperties;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefTokenService {

    private final CodefProperties codefProperties;
    private final CodefAuthClient codefAuthClient;

    private String accessToken;
    private LocalDateTime tokenExpirationTime;

    /**
     * 유효한 Access Token 반환
     */
    public synchronized String getAccessToken() {
        // 토큰이 없거나, 만료 시간이 지났거나(혹은 만료 5분 전이면) 재발급 수행
        if (this.accessToken == null || isTokenExpired()) {
            this.accessToken = fetchNewToken();
        }
        return this.accessToken;
    }

    /**
     * 토큰 만료 여부 확인
     */
    private boolean isTokenExpired() {
        if (tokenExpirationTime == null) {
            return true;
        }
        // 현재 시간이 (만료시간 - 5분) 보다 뒤에 있으면 만료로 간주
        return LocalDateTime.now().isAfter(tokenExpirationTime.minusMinutes(5));
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

                long expiresIn = 3600; // 기본값 1시간
                if (response.containsKey("expires_in")) {
                    Object expiresInObj = response.get("expires_in");
                    if (expiresInObj instanceof Number) {
                        expiresIn = ((Number) expiresInObj).longValue();
                    } else if (expiresInObj instanceof String) {
                        expiresIn = Long.parseLong((String) expiresInObj);
                    }
                }
                // 만료 시간 설정 (현재 시간 + 유효기간)
                this.tokenExpirationTime = LocalDateTime.now().plusSeconds(expiresIn);
                return (String) response.get("access_token");
            }
            throw new CodefException(CodefErrorCode.CODEF_TOKEN_ERROR);

        } catch (Exception e) {
            log.error("CODEF 토큰 발급 중 예외 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_TOKEN_ERROR);
        }
    }
}
