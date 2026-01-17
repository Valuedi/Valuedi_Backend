package org.umc.valuedi.infra.genai.config;

import com.google.genai.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GeminiConfig {

    private final GeminiProperties geminiProperties;

    @Bean
    public Client genaiClient() {
        return Client.builder()
                .apiKey(geminiProperties.getApiKey())
                .build();
    }
}
