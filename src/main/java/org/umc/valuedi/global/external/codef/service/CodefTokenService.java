package org.umc.valuedi.global.external.codef.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.client.CodefAuthClient;
import org.umc.valuedi.global.external.codef.config.CodefProperties;
import org.umc.valuedi.global.external.codef.dto.res.CodefTokenResDTO;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefTokenService {

    private static final String CODEF_ACCESS_TOKEN_KEY = "codef:access_token";
    private static final long TOKEN_VALIDITY_SECONDS = 7 * 24 * 60 * 60; // 1주일 (7일 * 24시간 * 60분 * 60초)
    private static final long TOKEN_EXPIRATION_BUFFER_SECONDS = 300; // 5분

    private final CodefProperties codefProperties;
    private final CodefAuthClient codefAuthClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 유효한 Access Token 반환
     */
    public synchronized String getAccessToken() {
        String token = stringRedisTemplate.opsForValue().get(CODEF_ACCESS_TOKEN_KEY);

        if (token != null) {
            return token;
        }

        log.info("CODEF Access Token이 만료되었거나 존재하지 않아 갱신을 시작합니다.");
        CodefTokenResDTO newTokenInfo = fetchNewToken();
        String newAccessToken = newTokenInfo.getAccessToken();
        
        // Redis에 토큰과 만료 시간(1주일 기준, 버퍼 적용) 저장
        long ttl = TOKEN_VALIDITY_SECONDS - TOKEN_EXPIRATION_BUFFER_SECONDS;
        stringRedisTemplate.opsForValue().set(CODEF_ACCESS_TOKEN_KEY, newAccessToken, ttl, TimeUnit.SECONDS);
        log.info("CODEF Access Token 발급 성공. Redis에 저장합니다. (TTL: {}s)", ttl);

        return newAccessToken;
    }

    /**
     * 토큰 재발급
     */
    private CodefTokenResDTO fetchNewToken() {
        try {
            String auth = codefProperties.getClientId() + ":" + codefProperties.getClientSecret();
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

            CodefTokenResDTO response = codefAuthClient.getAccessToken(
                    basicAuth,
                    "client_credentials",
                    "read"
            );

            if (response == null || response.getAccessToken() == null) {
                throw new CodefException(CodefErrorCode.CODEF_TOKEN_ERROR);
            }
            return response;

        } catch (FeignException e) {
            log.error("CODEF 토큰 발급 API 호출 실패 - Status: {}, Body: {}", e.status(), e.contentUTF8(), e);
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("CODEF 토큰 발급 API 호출 중 알 수 없는 오류 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_API_UNHANDLED_ERROR);
        }
    }
}
