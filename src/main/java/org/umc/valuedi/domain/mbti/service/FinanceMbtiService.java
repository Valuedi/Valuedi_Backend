package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public MemberMbtiTest submitTest(FinanceMbtiTestRequestDto req) {
        Long memberId = req.memberId();

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

        return memberMbtiTestRepository.save(test);
    }
}
