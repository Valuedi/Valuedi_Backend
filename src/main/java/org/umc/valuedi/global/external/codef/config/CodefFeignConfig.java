package org.umc.valuedi.global.external.codef.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}