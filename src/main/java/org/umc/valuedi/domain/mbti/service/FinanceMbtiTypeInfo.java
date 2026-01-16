package org.umc.valuedi.domain.mbti.service;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

import java.util.*;

@Component
public class FinanceMbtiTypeInfo implements FinanceMbtiProvider {

    private final EnumMap<MbtiType, FinanceMbtiTypeInfoDto> map = new EnumMap<>(MbtiType.class);

    public FinanceMbtiTypeInfo() {
        put(
                MbtiType.APGV,
                "일단 계획은 다 있어~형",
                "공격/불안/계획/회피 성향이 섞여 계획은 촘촘하지만 실행이 멈추기 쉬움.",
                List.of(
                        "계획에서 멈추고 실행을 안 할 수 있음",
                        "기회가 오면 계획을 무시하고 즉흥 행동할 수 있음"
                ),
                List.of(
                        "계획을 쪼개서 하나씩 행동해보기",
                        "투자는 신중히",
                        "완벽하지 않아도 일단 실행해보기"
                )
        );

        put(
                MbtiType.APGR,
                "묻고 더블로 가 형",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "불안해도 논리가 이기며 행동으로 이어짐",
                        "오래 생각해서 쉽게 피로해짐"
                ),
                List.of(
                        "손절 기준을 숫자로 계획하기"
                )
        );

        put(
                MbtiType.APCV,
                "계획에 살고 계획에 죽는다 형",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "계획 자체가 목적이된다.",
                        "행동이 후순위가 되는 경우가 있다",
                        "안정추구하며 성장이 밀리게 된다"
                ),
                List.of(
                        "계획후 실행으로 이동할 수 있는 장치를 마련한다",
                        "계획의 수를 줄여서 실행할 수 있게 한다"
                )
        );

        put(
                MbtiType.APCR,
                "수련하는 원칙주의자형",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "지나친 규칙이 때론 독이 될 수도",
                        " '계획이 없어서' 라는 말을 하면서 기회를 놓친다"
                ),
                List.of(
                        "유연성이 필요하다. 계획 변경은 실패가 아닌 더 좋은 방향으로 나아가기 위함임을 인지한다",
                        "계획에 예외항목을 미리 설정한다",
                        "분기별 점검 시행"
                )
        );

        put(
                MbtiType.AIGV,
                "나몰라잉 냅둬~",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "단기투자와 주식은 조심해야 한다",
                        "충동소비 빈도가 높다",
                        "손실을 마주하기 싫어 익절 시점을 놓치기 쉽다"
                ),
                List.of(
                        "투자 상한선을 고정해놓고 그 안에서 운용한다",
                        "자용 자금과/생활비 완전히 분리한다",
                        "해당 소비를 왜 하는지 생각해보고 최대한 글로 남겨본다"
                )
        );

        put(
                MbtiType.AICR,
                "지금은 때가 아니야~",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "지나친 안전추구로 성장기회 상실",
                        "‘지금을 때가 아니야'라는 생각으로 기회를 놓친다"
                ),
                List.of(
                        "자동화 시스템 이용하기",
                        "목표 달성률을 수치로 시각화"
                )
        );

        put(
                MbtiType.SPGV,
                "오케! 나머진 나중에!",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "계획만 있고 실행은 없다",
                        "손실에 대한 지각이 부족하다"
                ),
                List.of(
                        "계획을 세우면 실행할 날짜도입력하기",
                        "조그만 계획도 바로 실천하기",
                        "성공 경험을 많이 느껴봐야 해요"
                )
        );

        put(
                MbtiType.SPGR,
                "모든건 계획대로",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "과도한 자신감으로 외부 변수 과소평가",
                        "모든 선택을 계산하고 하다보니  피로 쉽게 누적",
                        "결과가 당연하다고 생각해요"
                ),
                List.of(
                        "전략을 ‘문서화’하고 정기적으로 검증하기",
                        "판단을 덜어주는 자동화 구조 활용"
                )
        );

        put(
                MbtiType.SPCV,
                "좋은게 좋은거지",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "계획을 세우지만 상태 유지를 위한 계획이에요",
                        "계획 업데이트가 미흡해요",
                        "장기 목표가 흐릿해요"
                ),
                List.of(
                        "목표를 정기적으로 재검토 해줘요"
                )
        );

        put(
                MbtiType.SPCR,
                "해탈한 원칙주의",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "성과 욕심이 적어요",
                        "도전이 부족해요",
                        "목표 상향이 없어요"
                ),
                List.of(
                        "달성 후 상향 목표로 조정해보아요"
                )
        );

        put(
                MbtiType.SIGV,
                "헐 바로 진행시켜!!",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "손실 반복 가능",
                        "리스크 가볍게 넘기는 성향",
                        "소비기준이 매번 달라짐"
                ),
                List.of(
                        "투자, 소비의 상한성 정해서 그 밑으로 사용",
                        "리스크 감수하는 기준 명확히 마련하기"
                )
        );

        put(
                MbtiType.SIGR,
                "내 감이 그래!",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "근거없는 자신감",
                        "논리로 공격적인 선택을 합리화 한다",
                        "소비를 반성하는 성향이 약하다"
                ),
                List.of(
                        "성공. 실패에 대해 깊이있는 리뷰가 필요하다",
                        "과잉소비 등 기준 이탈시 짧게라도 이유 기술"
                )
        );

        put(
                MbtiType.SICV,
                "귀찮핑",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "자산관리가 방치될 수 있다",
                        " 금융 관련해 에너지를 아끼는 타입"
                ),
                List.of(
                        "자동이체 필수",
                        "연말정산 등 필수적으로 챙겨야할것 위주로 따로 정리해두기",
                        "자주 관리 안해도 되는 상품 위주로 사용"
                )
        );

        put(
                MbtiType.SICR,
                "나 휩쓸려가유~",
                "공격/불안/계획/논리 조합으로 불안해도 논리로 밀어붙이며 행동으로 이어짐.",
                List.of(
                        "성장이 정체된다",
                        "개선, 비교 시도가 부족하다",
                        "장기적 관점에서의 계획이 부족"
                ),
                List.of(
                        "명확한 목표가 필요하다",
                        "무난함에 익숙해지지 않도록 경각심을 가지기"
                )
        );





    }

    private void put(
            MbtiType type,
            String title,
            String shortDesc,
            List<String> cautions,
            List<String> actions
    ) {
        map.put(type, new FinanceMbtiTypeInfoDto(type, title, shortDesc, cautions, actions));
    }

    @Override
    public FinanceMbtiTypeInfoDto get(MbtiType type) {
        FinanceMbtiTypeInfoDto dto = map.get(type);
        if (dto == null) {
            // 문구 누락 방지: 런타임에서 바로 잡히게
            throw new IllegalStateException("Finance MBTI type info not defined: " + type);
        }
        return dto;
    }

    @Override
    public List<FinanceMbtiTypeInfoDto> getAll() {
        // enum 순서대로 정렬해서 반환
        List<FinanceMbtiTypeInfoDto> result = new ArrayList<>();
        for (MbtiType t : MbtiType.values()) {
            result.add(get(t));
        }
        return result;
    }
}
