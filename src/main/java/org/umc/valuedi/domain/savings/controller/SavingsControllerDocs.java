package org.umc.valuedi.domain.savings.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Savings", description = "적금 상품 조회 API")
public interface SavingsControllerDocs {

    @Operation(summary = "적금 상품 목록 조회 API",
        description = "적금 상품 목록을 조회하는 API (현재는 금감원 OpenAPI 기반 전체 목록을 반환하여, 추후 금융 MBTI/제미나이 연동 시 추천 로직이 적용될 수 있습니다.)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "요청이 성공적으로 처리되었습니다.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SavingsListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS400_1", description = "금감원 API 요청 파라미터가 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS401_1", description =  "금감원 인증키가 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS403_1", description = "허용되지 않은 IP에서 요청했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS429_1", description = "금감원 API 일일 호출 한도를 초과했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS502_1", description = "금감원 API 내부 오류입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FSS502_2", description = "금감원 API 응답이 비어 있습니다.")
        })
    ResponseEntity<ApiResponse<SavingsListResponse>> findSavingsList();
}
