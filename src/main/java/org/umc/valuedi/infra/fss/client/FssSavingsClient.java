package org.umc.valuedi.infra.fss.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.umc.valuedi.infra.fss.config.FssProperties;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;

@Component
@RequiredArgsConstructor
public class FssSavingsClient {

    private final WebClient fssWebClient;
    private final FssProperties fssProperties;

    public FssSavingsResponse fetchSavingsList(int pageNo) {
        return fssWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/savingProductsSearch.json")
                        .queryParam("auth", fssProperties.getAuthKey())
                        .queryParam("topFinGrpNo", "020000")
                        .queryParam("pageNo", pageNo)
                        .build()
                )
                .retrieve()
                .bodyToMono(FssSavingsResponse.class)
                .block();
    }
}

