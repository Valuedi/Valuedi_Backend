package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationAsyncWorker {

    private final RecommendationService recommendationService;

    @Async("recommendationExecutor")
    public void generateAndSaveAsync(Long memberId) {
        recommendationService.generateAndSaveRecommendations(memberId);
    }
}

