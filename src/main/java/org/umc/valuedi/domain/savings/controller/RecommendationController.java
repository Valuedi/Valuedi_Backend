package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;
import org.umc.valuedi.domain.savings.service.RecommendationAsyncWorker;
import org.umc.valuedi.domain.savings.service.RecommendationService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequestMapping("/api/savings/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {

    private final RecommendationService recommendationService;
    private final RecommendationAsyncWorker recommendationAsyncWorker;

    // 추천 생성 트리거(비동기)
    @PostMapping
    public ApiResponse<SavingsResponseDTO.TriggerResponse> recommend(
            @CurrentMember Long memberId
    ) {
        recommendationAsyncWorker.generateAndSaveAsync(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.ACCEPTED, new SavingsResponseDTO.TriggerResponse(RecommendationStatus.PENDING, "추천 생성 요청이 접수되었습니다. 생성 완료까지 약간의 시간이 걸릴 수 있으며, 잠시 후 조회 API로 확인해 주세요."));
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
