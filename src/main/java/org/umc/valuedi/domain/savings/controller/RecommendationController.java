package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.RecommendationBatch;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;
import org.umc.valuedi.domain.savings.service.RecommendationAsyncWorker;
import org.umc.valuedi.domain.savings.service.RecommendationService;
import org.umc.valuedi.domain.savings.service.RecommendationTxService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequestMapping("/api/savings/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {

    private final RecommendationService recommendationService;
    private final RecommendationTxService recommendationTxService;
    private final RecommendationAsyncWorker recommendationAsyncWorker;

    // 추천 생성 트리거(비동기)
    @PostMapping
    public ApiResponse<SavingsResponseDTO.TriggerResponse> recommend(
            @CurrentMember Long memberId
    ) {
        SavingsResponseDTO.TriggerDecision triggerDecision = recommendationTxService.triggerRecommendation(memberId);

        // 진행 중이면 새로 실행하지 않음
        if (triggerDecision.shouldStartAsync()) {
            recommendationAsyncWorker.generateAndSaveAsync(memberId, triggerDecision.batchId());
        }

        return ApiResponse.onSuccess(GeneralSuccessCode.ACCEPTED,
                SavingsResponseDTO.TriggerResponse.builder()
                        .batchId(triggerDecision.batchId())
                        .status(triggerDecision.status())
                        .message(triggerDecision.message())
                        .build()
        );
    }

    // 최신 추천 15개 조회
    @GetMapping
    public ApiResponse<SavingsResponseDTO.SavingsListResponse> latest15(
            @RequestParam(required = false) String rsrvType,
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, recommendationService.getRecommendation(memberId, rsrvType));
    }

    // 최신 추천 Top3 조회
    @GetMapping("/top3")
    public ApiResponse<SavingsResponseDTO.SavingsListResponse> latestTop3(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, recommendationService.getRecommendationTop3(memberId));
    }

    // 추천 상품 상세 조회
    @GetMapping("/{finPrdtCd}")
    public ApiResponse<SavingsResponseDTO.SavingsDetailResponse> findSavingsDetail(
            @PathVariable String finPrdtCd
    ) {
        SavingsResponseDTO.SavingsDetailResponse result = recommendationService.getSavingsDetail(finPrdtCd);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }
}
