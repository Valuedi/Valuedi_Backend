package org.umc.valuedi.domain.mbti.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.enums.MbtiQuestionCategory;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceMbtiScoringService {

    private final FinanceMbtiProvider typeInfoProvider;

    public record ScoreResult(
            int anxietyScore,
            int stabilityScore,
            int impulseScore,
            int planningScore,
            int aggressiveScore,
            int conservativeScore,
            int avoidanceScore,
            int rationalScore,
            MbtiType resultType
    ) {}

    public ScoreResult score(List<MbtiQuestion> questions, Map<Long, Integer> answersByQuestionId) {
        int anxiety = 0, stability = 0;
        int impulse = 0, planning = 0;
        int aggressive = 0, conservative = 0;
        int avoidance = 0, rational = 0;

        for (MbtiQuestion q : questions) {
            Integer choice = answersByQuestionId.get(q.getId());
            if (choice == null) continue;

            int w = 3 - choice; // 1~5 -> +2,+1,0,-1,-2

            MbtiQuestionCategory cat = q.getCategory();
            switch (cat) {
                case ANXIETY_STABILITY -> { if (w >= 0) anxiety += w; else stability += -w; }
                case IMPULSE_PLANNING -> { if (w >= 0) impulse += w; else planning += -w; }
                case AGGRESSIVE_CONSERVATIVE -> { if (w >= 0) aggressive += w; else conservative += -w; }
                case AVOIDANCE_RATIONAL -> { if (w >= 0) avoidance += w; else rational += -w; }
            }
        }

        MbtiType type = MbtiType.fromScores(
                anxiety, stability,
                impulse, planning,
                aggressive, conservative,
                avoidance, rational
        );

        return new ScoreResult(
                anxiety, stability,
                impulse, planning,
                aggressive, conservative,
                avoidance, rational,
                type
        );
    }

    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        return typeInfoProvider.getAll();
    }

    public FinanceMbtiTypeInfoDto getTypeInfo(MbtiType type) {
        return typeInfoProvider.get(type);
    }
}
