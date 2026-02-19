package org.umc.valuedi.domain.savings.service;

import org.umc.valuedi.domain.mbti.enums.MbtiType;

/**
 * MBTI 타입을 추천 후보 쿼리 파라미터로 변환하는 값 객체.
 *
 * 축별 매핑:
 *  - I(충동) → saveTrm <= 12  /  P(계획) → saveTrm >= 24
 *  - G(공격) → intrRate2 우선 정렬  /  C(보수) → intrRate 우선 정렬
 *  - V(회피) → rsrvType = 'S'(정기적립)  /  R(합리) → 무관
 *  - A/S 축은 I/P 축에 흡수
 */
public record MbtiCandidateFilter(
        String rsrvType,          // null=무관, "S"=정기적립
        Integer minTrm,           // null=무관, 24=24개월 이상
        Integer maxTrm,           // null=무관, 12=12개월 이하
        boolean orderByRate2First // true=우대금리순, false=기본금리순
) {
    public static MbtiCandidateFilter from(MbtiType mbtiType) {
        String name = mbtiType.name(); // e.g. "APGV"
        char axis2 = name.charAt(1);  // I or P
        char axis3 = name.charAt(2);  // G or C
        char axis4 = name.charAt(3);  // V or R

        String rsrvType      = (axis4 == 'V') ? "S" : null;
        Integer minTrm       = (axis2 == 'P') ? 24  : null;
        Integer maxTrm       = (axis2 == 'I') ? 12  : null;
        boolean orderByRate2 = (axis3 == 'G');

        return new MbtiCandidateFilter(rsrvType, minTrm, maxTrm, orderByRate2);
    }
}
