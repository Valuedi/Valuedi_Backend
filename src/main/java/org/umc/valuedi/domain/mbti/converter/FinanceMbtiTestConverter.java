package org.umc.valuedi.domain.mbti.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiResponse;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.service.FinanceMbtiScoringService;
import org.umc.valuedi.domain.member.entity.Member;

import java.util.Map;

@Component
public class FinanceMbtiTestConverter {

    public MemberMbtiTest toEntity(
            Member member,
            FinanceMbtiTestRequestDto req,
            FinanceMbtiScoringService.ScoreResult score,
            Map<Long, MbtiQuestion> questionMap
    ) {
        MemberMbtiTest test = MemberMbtiTest.builder()
                .member(member)
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
            MbtiQuestion q = questionMap.get(a.questionId());
            MemberMbtiResponse response = MemberMbtiResponse.builder()
                    .question(q)
                    .choiceValue(a.choiceValue())
                    .build();
            test.addResponse(response);
        }

        return test;
    }
}
