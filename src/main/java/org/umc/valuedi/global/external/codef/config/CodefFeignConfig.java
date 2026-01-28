package org.umc.valuedi.global.external.codef.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.umc.valuedi.global.external.codef.service.CodefTokenService;

public class CodefFeignConfig {

    @Bean
    public RequestInterceptor codefAuthInterceptor(CodefTokenService tokenService) {
        return new CodefAuthInterceptor(tokenService);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Decoder codefDecoder() {
        return new CodefResponseDecoder();
    }

    @Bean
    public Retryer retryer() {
        // period: 100ms (초기 대기 시간)
        // maxPeriod: 1000ms (최대 대기 시간)
        // maxAttempts: 3 (최대 시도 횟수 - 최초 시도 포함)
        return new Retryer.Default(100, 1000, 3);
    }
}
