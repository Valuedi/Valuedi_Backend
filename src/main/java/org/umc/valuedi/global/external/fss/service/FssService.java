package org.umc.valuedi.global.external.fss.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.umc.valuedi.global.external.fss.dto.FssSavingResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FssService {
    private final WebClient fssWebClient;

    @Value("${fss.api.key}")
    private String apiKey;

    public Mono<String> fetchRawData(String topFinGrpNo) {
        return fssWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/savingProductsSearch.json")
                        .queryParam("auth", apiKey)
                        .queryParam("topFinGrpNo", topFinGrpNo)
                        .queryParam("pageNo", "1")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}
