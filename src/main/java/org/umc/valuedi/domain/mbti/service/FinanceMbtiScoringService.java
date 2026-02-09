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

    private static final double BASE_SCORE = 10.0;

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
        // 모든 점수를 BASE_SCORE(10.0)에서 시작하도록 변경
        double anxiety = BASE_SCORE, stability = BASE_SCORE;
        double impulse = BASE_SCORE, planning = BASE_SCORE;
        double aggressive = BASE_SCORE, conservative = BASE_SCORE;
        double avoidance = BASE_SCORE, rational = BASE_SCORE;

        for (MbtiQuestion q : questions) {
            Integer choice = answersByQuestionId.get(q.getId());
            if (choice == null) continue;

            // DB에 추가한 가중치(weight) 적용 (없을 경우 1)
            double weight = (q.getWeight() != null) ? q.getWeight() : 1.0;
            double point = (3 - choice) * weight;

            MbtiQuestionCategory cat = q.getCategory();
            switch (cat) {
                case ANXIETY_STABILITY -> { if (point >= 0) anxiety += point; else stability += -point; }
                case IMPULSE_PLANNING -> { if (point >= 0) impulse += point; else planning += -point; }
                case AGGRESSIVE_CONSERVATIVE -> { if (point >= 0) aggressive += point; else conservative += -point; }
                case AVOIDANCE_RATIONAL -> { if (point >= 0) avoidance += point; else rational += -point; }
            }
        }

        // 최종 결과 타입 결정 (실수를 정수로 캐스팅)
        MbtiType type = MbtiType.fromScores(
                (int)anxiety, (int)stability,
                (int)impulse, (int)planning,
                (int)aggressive, (int)conservative,
                (int)avoidance, (int)rational
        );

        return new ScoreResult(
                (int)anxiety, (int)stability,
                (int)impulse, (int)planning,
                (int)aggressive, (int)conservative,
                (int)avoidance, (int)rational,
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
