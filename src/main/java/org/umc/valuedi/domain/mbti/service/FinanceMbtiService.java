package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiResponse;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.exception.MbtiException;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.mbti.repository.MbtiQuestionRepository;
import org.umc.valuedi.domain.mbti.validation.FinanceMbtiTestValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceMbtiService {

    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final MbtiQuestionRepository mbtiQuestionRepository;
    private final FinanceMbtiScoringService scoringService;
    private final FinanceMbtiTestValidator financeMbtiTestValidator;

    @Transactional(readOnly = true)
    public List<MbtiQuestion> getActiveQuestions() {
        return mbtiQuestionRepository.findActiveQuestions();
    }

    // 테스트 제출
    @Transactional
    public MemberMbtiTest submitTest(FinanceMbtiTestRequestDto req) {
        Long memberId = req.memberId();

        List<MbtiQuestion> activeQuestions = mbtiQuestionRepository.findActiveQuestions();

        // 답변 검증 (중복, null, 범위 등)
        Map<Long, MbtiQuestion> activeQuestionMap =
                financeMbtiTestValidator.validateAndBuildQuestionMap(req, activeQuestions);

        memberMbtiTestRepository.deactivateAllActiveTests(memberId);

        Map<Long, Integer> answersByQuestionId = req.answers().stream()
                .collect(Collectors.toMap(
                        FinanceMbtiTestRequestDto.Answer::questionId,
                        FinanceMbtiTestRequestDto.Answer::choiceValue
                ));

        // 점수 계산
        FinanceMbtiScoringService.ScoreResult score =
                scoringService.score(activeQuestions, answersByQuestionId);

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

        // 응답 엔티티 연결
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
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.NO_ACTIVE_RESULT));
    }

    // 결과 유형 조회 (16유형 목록)
    @Transactional(readOnly = true)
    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        return scoringService.getTypeInfos();
    }
}
