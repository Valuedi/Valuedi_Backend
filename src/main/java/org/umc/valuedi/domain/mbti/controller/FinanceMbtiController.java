package org.umc.valuedi.domain.mbti.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.mbti.dto.*;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.service.FinanceMbtiService;
import org.umc.valuedi.domain.mbti.service.query.FinanceMbtiQueryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finance-mbti")
public class FinanceMbtiController implements FinanceControllerDocs{

    private final FinanceMbtiQueryService financeMbtiQueryService;
    private final FinanceMbtiService financeMbtiCommandService;

    // 1) 문항 조회

    @GetMapping("/questions")
    public List<MbtiQuestionResponseDto> getQuestions() {
        return financeMbtiQueryService.getActiveQuestions().stream()
                .map(MbtiQuestionResponseDto::from)
                .toList();
    }

    // 2) 테스트 제출
    @PostMapping("/test")
    public FinanceMbtiTestResultResponseDto submitTest(@Valid @RequestBody FinanceMbtiTestRequestDto req) {
        MemberMbtiTest saved = financeMbtiCommandService.submitTest(req);
        return FinanceMbtiTestResultResponseDto.from(saved);
    }

    // 3) 결과 조회 (대표 결과)
    @GetMapping("/result")
    public FinanceMbtiTestResultResponseDto getResult(@RequestParam Long memberId) {
        return FinanceMbtiTestResultResponseDto.from(financeMbtiQueryService.getCurrentResult(memberId));
    }

    // 4) 결과 유형 조회 (16유형 목록)
    @GetMapping("/result/type")
    public List<FinanceMbtiTypeInfoDto> getResultTypes() {
        return financeMbtiQueryService.getTypeInfos();
    }
}
