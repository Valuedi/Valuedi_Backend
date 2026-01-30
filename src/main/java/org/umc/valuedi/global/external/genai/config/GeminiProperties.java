package org.umc.valuedi.global.external.genai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "genai.gemini")
public class GeminiProperties {

    private String apiKey;
    private String model;
}
