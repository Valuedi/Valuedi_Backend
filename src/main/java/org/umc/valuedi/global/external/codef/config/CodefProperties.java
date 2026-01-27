package org.umc.valuedi.global.external.codef.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "codef")
public class CodefProperties {
    private String clientId;
    private String clientSecret;
    private String publicKey;
    private String apiUrl;
    private String aesSecret;
}