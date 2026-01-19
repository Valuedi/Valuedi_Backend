package org.umc.valuedi.global.external.codef.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.umc.valuedi.global.external.codef.service.CodefTokenService;

@Slf4j
@RequiredArgsConstructor
public class CodefAuthInterceptor implements RequestInterceptor {

    private final CodefTokenService tokenService;

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenService.getAccessToken();
        template.header("Authorization", "Bearer " + token);
        template.header("Content-Type", "application/json");
    }
}
