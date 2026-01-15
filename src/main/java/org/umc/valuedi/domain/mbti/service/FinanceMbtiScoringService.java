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

    /**
     * answersByQuestionId: questionId -> choiceValue(1~5)
     */
    public ScoreResult score(List<MbtiQuestion> questions, Map<Long, Integer> answersByQuestionId) {
        int anxiety = 0, stability = 0;
        int impulse = 0, planning = 0;
        int aggressive = 0, conservative = 0;
        int avoidance = 0, rational = 0;

        for (MbtiQuestion q : questions) {
            Integer choice = answersByQuestionId.get(q.getId());
            if (choice == null) continue; // (정책에 따라 throw로 바꿔도 됨)

            // 1~5 -> +2,+1,0,-1,-2 (앞 성향 강도)
            int w = 3 - choice;

            MbtiQuestionCategory cat = q.getCategory();
            switch (cat) {
                case ANXIETY_STABILITY -> {
                    if (w >= 0) anxiety += w;
                    else stability += -w;
                }
                case IMPULSE_PLANNING -> {
                    if (w >= 0) impulse += w;
                    else planning += -w;
                }
                case AGGRESSIVE_CONSERVATIVE -> {
                    if (w >= 0) aggressive += w;
                    else conservative += -w;
                }
                case AVOIDANCE_RATIONAL -> {
                    if (w >= 0) avoidance += w;
                    else rational += -w;
                }
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

    /**
     * 16유형 설명(주의할 점/추천행동)을 "DB 없이 코드로" 제공.
     * (원하는 문구로 너네 서비스 톤에 맞게 바꿔도 됨)
     */
    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        List<FinanceMbtiTypeInfoDto> result = new ArrayList<>();
        for (MbtiType t : MbtiType.values()) {
            result.add(typeInfoOf(t));
        }
        return result;
    }

    private FinanceMbtiTypeInfoDto typeInfoOf(MbtiType t) {
        // 아주 간단한 기본값 템플릿(필요하면 유형별로 분기해서 더 구체화 가능)
        String title = t.name(); // 예: AIGV
        String shortDesc = switch (t) {
            case AIGV -> "불안/충동/공격/회피 성향이 강한 편.\n감정 기반 의사결정이 빠르게 굳어질 수 있음.";
            case SPCR -> "안정/계획/보수/이성 성향이 강한 편.\n리스크 통제와 장기 전략에 강점이 있음.";
            default -> "재무 의사결정에서 특정 성향 조합이 두드러짐.\n강점을 살리고 약점을 보완하는 전략이 중요함.";
        };

        List<String> cautions = switch (t) {
            case AIGV -> List.of(
                    "손실 회피/불안으로 인해 성급한 매도·매수 반복 주의",
                    "감정이 올라온 상태에서 의사결정 내리지 않기",
                    "과도한 확신(공격성)으로 레버리지 확대 주의"
            );
            case SPCR -> List.of(
                    "너무 보수적으로만 접근해 기회비용이 커지지 않게 주의",
                    "정보 수집이 과해 의사결정이 늦어지지 않게 주의",
                    "계획이 깨졌을 때 대안 플랜(Plan B) 마련"
            );
            default -> List.of(
                    "한 번의 결과로 패턴을 단정하지 않기",
                    "단기 변동에 과민반응하지 않기",
                    "본인 성향이 강하게 드러나는 상황(급락/급등 등)에서 체크리스트 사용"
            );
        };

        List<String> actions = switch (t) {
            case AIGV -> List.of(
                    "매수/매도 전 10분 쿨다운(알람) 적용",
                    "손절·익절·분할매수 규칙을 숫자로 미리 고정",
                    "월 1회만 포트폴리오 리밸런싱(빈도 제한)"
            );
            case SPCR -> List.of(
                    "핵심 지표 3개만 정해 빠르게 판단(과분석 방지)",
                    "소액으로 새로운 전략을 테스트하는 ‘샌드박스’ 계좌 운영",
                    "분기 단위 목표(수익률/변동성/현금비중)로 점검"
            );
            default -> List.of(
                    "가계부/자산 대시보드로 ‘현황 인지’ 루틴 만들기",
                    "의사결정 로그(왜 샀는지/왜 팔았는지) 남기기",
                    "본인 성향의 약점을 보완하는 자동 규칙(정기적립, 자동이체 등) 적용"
            );
        };

        return new FinanceMbtiTypeInfoDto(t, title, shortDesc, cautions, actions);
    }
}
