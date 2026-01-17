package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiResponse;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.mbti.repository.MbtiQuestionRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceMbtiService {

    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final MbtiQuestionRepository mbtiQuestionRepository;
    private final FinanceMbtiScoringService scoringService;

    @Transactional(readOnly = true)
    public List<MbtiQuestion> getActiveQuestions() {
        return mbtiQuestionRepository.findActiveQuestions();
    }


    //테스트 제출
    @Transactional
    public MemberMbtiTest submitTest(FinanceMbtiTestRequestDto req) {
        Long memberId = req.memberId();

        // 활성화된 문항 조회
        List<MbtiQuestion> activeQuestions = mbtiQuestionRepository.findActiveQuestions();
        if (activeQuestions.isEmpty()) {
            throw new IllegalStateException("활성화된 MBTI 문항이 없습니다.");
        }

        Map<Long, MbtiQuestion> activeQuestionMap = activeQuestions.stream()
                .collect(Collectors.toMap(MbtiQuestion::getId, Function.identity()));

        // 중복 answer 체크
        long distinctCount = req.answers().stream()
                .map(FinanceMbtiTestRequestDto.Answer::questionId)
                .distinct()
                .count();

        if (distinctCount != req.answers().size()) {
            throw new IllegalArgumentException("answers에 중복 questionId가 있습니다.");
        }

        // answers 개수 검증
        if (req.answers().size() != activeQuestions.size()) {
            throw new IllegalArgumentException(
                    "모든 문항에 답변해야 합니다. (필요: " + activeQuestions.size() + "개, 제출: " + req.answers().size() + "개)"
            );
        }

        for (FinanceMbtiTestRequestDto.Answer a : req.answers()) {
            if (!activeQuestionMap.containsKey(a.questionId())) {
                throw new IllegalArgumentException("유효하지 않은 questionId가 포함되어 있습니다: " + a.questionId());
            }

            Integer choiceValue = a.choiceValue();
            if (choiceValue == null || choiceValue < 1 || choiceValue > 5) {
                throw new IllegalArgumentException(
                        "choiceValue는 1~5 사이의 정수여야 합니다. (questionId="
                                + a.questionId()
                                + ", value="
                                + choiceValue
                                + ")"
                );
            }
        }

        // 기존 결과 비활성화
        memberMbtiTestRepository.deactivateAllActiveTests(memberId);

        // answers map
        Map<Long, Integer> answersByQuestionId = req.answers().stream()
                .collect(Collectors.toMap(
                        FinanceMbtiTestRequestDto.Answer::questionId,
                        FinanceMbtiTestRequestDto.Answer::choiceValue
                ));

        // 점수 계산
        FinanceMbtiScoringService.ScoreResult score = scoringService.score(activeQuestions, answersByQuestionId);

        // 테스트 엔티티 생성
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
                .isActive(true)
                .build();


        for (FinanceMbtiTestRequestDto.Answer a : req.answers()) {
            MbtiQuestion q = activeQuestionMap.get(a.questionId());
            MemberMbtiResponse response = MemberMbtiResponse.builder()
                    .question(q)
                    .choiceValue(a.choiceValue())
                    .build();
            test.addResponse(response);
        }

        return memberMbtiTestRepository.save(test);
    }


     // MBTI 결과 조회

    @Transactional(readOnly = true)
    public MemberMbtiTest getCurrentResult(Long memberId) {
        return memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new IllegalStateException("No active MBTI test result for memberId=" + memberId));
    }

     // 결과 유형 조회 (16유형 목록)
    @Transactional(readOnly = true)
    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        return scoringService.getTypeInfos();
    }
}
