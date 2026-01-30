package org.umc.valuedi.domain.mbti.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.umc.valuedi.domain.mbti.dto.*;

import java.util.List;

@Tag(name = "Finance MBTI", description = "금융 MBTI 테스트/결과 API")
public interface FinanceControllerDocs {

    @Operation(
            summary = "금융 MBTI 문항 조회 API",
            description = "현재 활성화된 금융 MBTI 문항 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "활성화된 문항이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<List<MbtiQuestionResponseDto>> getQuestions();


    @Operation(
            summary = "금융 MBTI 테스트 제출 API",
            description = "문항에 대한 응답을 제출하고, 점수를 계산하여 결과를 리턴합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (중복 questionId, 누락 응답, choiceValue 범위 오류 등)"),
            @ApiResponse(responseCode = "404", description = "활성화된 문항이 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<FinanceMbtiTestResultResponseDto> submitTest(
            @Valid @RequestBody FinanceMbtiTestRequestDto req
    );


    @Operation(
            summary = "사용자의 금융 MBTI 결과 조회 API",
            description = "memberId의 현재 금융 MBTI 대표 결과(가장 최근 검사 결과)를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "활성화된 결과가 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<FinanceMbtiTestResultResponseDto> getResult(
            @Parameter(description = "회원 ID", example = "1", required = true)
            @RequestParam Long memberId
    );


    @Operation(
            summary = "금융 MBTI 결과 유형(16유형) 목록 조회 API",
            description = "금융 MBTI 16유형의 상세 정보 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "유형 정보가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<List<FinanceMbtiTypeInfoDto>> getResultTypes();
}
