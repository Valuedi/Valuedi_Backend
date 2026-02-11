package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.mbti.converter.FinanceMbtiTestConverter;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.mbti.repository.MbtiQuestionRepository;
import org.umc.valuedi.domain.mbti.validator.FinanceMbtiTestValidator;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.savings.service.RecommendationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinanceMbtiService {

    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final MemberRepository memberRepository;
    private final MbtiQuestionRepository mbtiQuestionRepository;
    private final FinanceMbtiScoringService scoringService;
    private final FinanceMbtiTestValidator financeMbtiTestValidator;
    private final FinanceMbtiTestConverter financeMbtiTestConverter;
    private final RecommendationService recommendationService;

    public MemberMbtiTest submitTest(Long memberId, FinanceMbtiTestRequestDto req) {

        Member member = memberRepository.getReferenceById(memberId);

        List<MbtiQuestion> activeQuestions = mbtiQuestionRepository.findAllByOrderByIdAsc();

        Map<Long, MbtiQuestion> activeQuestionMap = financeMbtiTestValidator.validateAndBuildQuestionMap(req, activeQuestions);

        memberMbtiTestRepository.deactivateAllActiveTests(memberId);

        Map<Long, Integer> answersByQuestionId = req.answers().stream()
                .collect(Collectors.toMap(
                        FinanceMbtiTestRequestDto.Answer::questionId,
                        FinanceMbtiTestRequestDto.Answer::choiceValue
                ));

        FinanceMbtiScoringService.ScoreResult score = scoringService.score(activeQuestions, answersByQuestionId);
        MemberMbtiTest test = financeMbtiTestConverter.toEntity(member, req, score, activeQuestionMap);

        MemberMbtiTest savedTest = memberMbtiTestRepository.save(test);

        try {
            recommendationService.generateAndSaveRecommendations(memberId);
            log.info("[Recommend] MBTI 검사 후 자동 추천 생성 성공. memberId={}", memberId);
        } catch (Exception e) {
            // 제미나이 호출이 실패해도 MBTI 저장은 유지되도록 예외를 삼키고 로그만 남김
            log.error("[Recommend] MBTI 저장에는 성공했으나, 자동 추천 생성 중 오류 발생. memberId={}: {}", memberId, e.getMessage());
        }
        return savedTest;
    }
}
