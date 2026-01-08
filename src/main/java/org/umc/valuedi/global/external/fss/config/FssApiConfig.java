package org.umc.valuedi.global.external.fss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FssApiConfig {

    @Bean
    public WebClient fssWebClient() {
        return WebClient.builder()
                .baseUrl("https://finlife.fss.or.kr/finlifeapi")
                .build();
    }
}
