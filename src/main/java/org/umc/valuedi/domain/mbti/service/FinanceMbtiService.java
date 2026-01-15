package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiResponse;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.repository.MbtiQuestionRepository;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceMbtiService {

    private final MbtiQuestionRepository mbtiQuestionRepository;
    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final FinanceMbtiScoringService scoringService;

    @Transactional(readOnly = true)
    public List<MbtiQuestion> getActiveQuestions() {
        return mbtiQuestionRepository.findActiveQuestions();
    }

    @Transactional
    public MemberMbtiTest submitTest(FinanceMbtiTestRequestDto req) {
        Long memberId = req.memberId();

        List<MbtiQuestion> activeQuestions = mbtiQuestionRepository.findActiveQuestions();
        if (activeQuestions.isEmpty()) {
            throw new IllegalStateException("No active MBTI questions found.");
        }

        // questionId -> question
        Map<Long, MbtiQuestion> questionMap = activeQuestions.stream()
                .collect(Collectors.toMap(MbtiQuestion::getId, Function.identity()));

        Map<Long, Integer> answersByQid = new HashMap<>();
        for (FinanceMbtiTestRequestDto.Answer a : req.answers()) {
            if (!questionMap.containsKey(a.questionId())) {
                throw new IllegalArgumentException("Invalid questionId: " + a.questionId());
            }
            if (answersByQid.put(a.questionId(), Integer.valueOf(a.choiceValue())) != null) {
                throw new IllegalArgumentException("Duplicated questionId: " + a.questionId());
            }
        }

        if (answersByQid.size() != activeQuestions.size()) {
            throw new IllegalArgumentException(
                    "Answer count mismatch. expected=" + activeQuestions.size() + ", actual=" + answersByQid.size()
            );
        }

        // 점수 산출
        FinanceMbtiScoringService.ScoreResult score = scoringService.score(activeQuestions, answersByQid);

        // 기존 대표 테스트 비활성화(대표는 1개만 유지)
        memberMbtiTestRepository.deactivateAllActiveTests(memberId);

        // 새 테스트 생성
        MemberMbtiTest test = MemberMbtiTest.builder()
                .memberId(memberId)
                .resultType(score.resultType())
                .anxietyScore(score.anxietyScore())
                .stabilityScore(score.stabilityScore())
                .impulseScore(score.impulseScore())
                .planningScore(score.planningScore())
                .aggressiveScore(score.aggressiveScore())
                .conservativeScore(score.conservativeScore())
                .avoidanceScore(score.avoidanceScore())
                .rationalScore(score.rationalScore())
                .isActive(true) // 대표 결과로 저장
                .build();

        // 응답 저장(연관관계 세팅)
        for (var entry : answersByQid.entrySet()) {
            MbtiQuestion q = questionMap.get(entry.getKey());
            Integer choiceVal = entry.getValue();

            MemberMbtiResponse resp = MemberMbtiResponse.builder()
                    .question(q)
                    .choiceValue((int) choiceVal.byteValue()) // Integer -> Byte
                    .build();

            test.addResponse(resp); // cascade=ALL 이라 test 저장 시 response도 저장됨
        }

        return memberMbtiTestRepository.save(test);
    }

    @Transactional(readOnly = true)
    public MemberMbtiTest getCurrentResult(Long memberId) {
        return memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new NoSuchElementException("No active MBTI test result for memberId=" + memberId));
    }

    @Transactional(readOnly = true)
    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        return scoringService.getTypeInfos();
    }
}
