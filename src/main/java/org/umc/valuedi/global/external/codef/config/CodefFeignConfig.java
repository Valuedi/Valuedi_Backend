package org.umc.valuedi.global.external.codef.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.umc.valuedi.global.external.codef.service.CodefTokenService;

import java.util.concurrent.TimeUnit;

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
    public Request.Options options() {
        return new Request.Options(
                10, TimeUnit.SECONDS,
                3, TimeUnit.MINUTES,
                true
        );
    }
}