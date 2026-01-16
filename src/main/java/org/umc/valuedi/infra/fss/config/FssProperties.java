package org.umc.valuedi.infra.fss.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fss")
public class FssProperties {

    private String baseUrl;
    private String authKey;
}
