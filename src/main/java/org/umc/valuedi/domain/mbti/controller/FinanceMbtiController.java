package org.umc.valuedi.domain.mbti.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.mbti.dto.*;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.exception.code.MbtiSuccessCode;
import org.umc.valuedi.domain.mbti.service.FinanceMbtiService;
import org.umc.valuedi.domain.mbti.service.query.FinanceMbtiQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finance-mbti")
public class FinanceMbtiController implements FinanceControllerDocs {

    private final FinanceMbtiQueryService financeMbtiQueryService;
    private final FinanceMbtiService financeMbtiCommandService;

    // 1) 문항 조회
    @GetMapping("/questions")
    public ApiResponse<List<MbtiQuestionResponseDto>> getQuestions() {
        List<MbtiQuestionResponseDto> result = financeMbtiQueryService.getActiveQuestions().stream()
                .map(MbtiQuestionResponseDto::from)
                .toList();

        return ApiResponse.onSuccess(
                MbtiSuccessCode.MBTI_QUESTIONS_FETCHED,
                result
        );
    }

    // 2) 테스트 제출
    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FinanceMbtiTestResultResponseDto> submitTest(
            @Valid
            @RequestBody FinanceMbtiTestRequestDto req
    ) {
        MemberMbtiTest saved = financeMbtiCommandService.submitTest(req);

        return ApiResponse.onSuccess(
                MbtiSuccessCode.MBTI_TEST_SUBMITTED,
                FinanceMbtiTestResultResponseDto.from(saved)
        );
    }

    // 3) 결과 조회 (대표 결과)
    @GetMapping("/result")
    public ApiResponse<FinanceMbtiTestResultResponseDto> getResult(
            @RequestParam Long memberId
    ) {
        return ApiResponse.onSuccess(
                MbtiSuccessCode.MBTI_RESULT_FETCHED,
                FinanceMbtiTestResultResponseDto.from(financeMbtiQueryService.getCurrentResult(memberId))
        );
    }

    // 4) 결과 유형 조회 (16유형 목록)
    @GetMapping("/result/type")
    public ApiResponse<List<FinanceMbtiTypeInfoDto>> getResultTypes() {
        return ApiResponse.onSuccess(
                MbtiSuccessCode.MBTI_TYPE_LIST_FETCHED,
                financeMbtiQueryService.getTypeInfos()
        );
    }
}
