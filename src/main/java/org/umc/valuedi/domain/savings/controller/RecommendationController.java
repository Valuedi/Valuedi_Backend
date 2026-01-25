package org.umc.valuedi.domain.savings.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.savings.dto.request.SavingsRequestDTO;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.service.RecommendationService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

@RestController
@RequestMapping("/api/savings/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // 15개 추천 생성 + 저장 + 응답(15개)
    @PostMapping
    public ApiResponse<SavingsResponseDTO.RecommendResponse> recommend(
            @RequestBody @Valid SavingsRequestDTO.RecommendRequest request
    ) {
        SavingsResponseDTO.RecommendResponse result = recommendationService.recommend(request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    // 최신 추천 15개 조회
    @GetMapping
    public ApiResponse<SavingsResponseDTO.SavingsListResponse> latest15(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, recommendationService.getRecommendation(memberId));
    }

    // 최신 추천 Top3 조회
    @GetMapping("/top3")
    public ApiResponse<SavingsResponseDTO.SavingsListResponse> latestTop3(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, recommendationService.getRecommendationTop3(memberId));
    }
}
