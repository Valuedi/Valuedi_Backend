package org.umc.valuedi.global.external.codef.config;

import io.codef.api.EasyCodef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EasyCodefConfig {

    @Value("${codef.client-id}")
    private String clientId;

    @Value("${codef.client-secret}")
    private String clientSecret;

    @Value("${codef.public-key}")
    private String publicKey;

    @Bean
    public EasyCodef easyCodef() {
        EasyCodef codef = new EasyCodef();
        codef.setClientInfoForDemo(clientId, clientSecret);
        codef.setPublicKey(publicKey);
        return codef;
    }
}