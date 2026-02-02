package org.umc.valuedi.global.external.fss.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class FssWebClientConfig {

    private final FssProperties fssProperties;

    @Bean
    public WebClient fssWebClient() {
        return WebClient.builder()
                .baseUrl(fssProperties.getBaseUrl())
                .build();
    }
}
