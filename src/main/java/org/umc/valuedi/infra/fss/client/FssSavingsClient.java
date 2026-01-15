package org.umc.valuedi.infra.fss.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;

@Component
@RequiredArgsConstructor
public class FssSavingsClient {

    private final WebClient fssWebClient;

    @Value("${fss.auth-key}")
    private String authKey;

    public FssSavingsResponse fetchSavingsList(int pageNo) {
        return fssWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/savingProductsSearch.json")
                        .queryParam("auth", authKey)
                        .queryParam("topFinGrpNo", "020000")
                        .queryParam("pageNo", pageNo)
                        .build()
                )
                .retrieve()
                .bodyToMono(FssSavingsResponse.class)
                .block();
    }
}

