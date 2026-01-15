package org.umc.valuedi.domain.mbti.dto;

import org.umc.valuedi.domain.mbti.enums.MbtiType;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;

import java.time.LocalDateTime;

public record FinanceMbtiTestResultResponseDto(
        Long testId,
        Long memberId,
        MbtiType resultType,
        int anxietyScore,
        int stabilityScore,
        int impulseScore,
        int planningScore,
        int aggressiveScore,
        int conservativeScore,
        int avoidanceScore,
        int rationalScore,
        LocalDateTime createdAt
) {
    public static FinanceMbtiTestResultResponseDto from(MemberMbtiTest t) {
        return new FinanceMbtiTestResultResponseDto(
                t.getId(),
                t.getMemberId(),
                t.getResultType(),
                t.getAnxietyScore(),
                t.getStabilityScore(),
                t.getImpulseScore(),
                t.getPlanningScore(),
                t.getAggressiveScore(),
                t.getConservativeScore(),
                t.getAvoidanceScore(),
                t.getRationalScore(),
                t.getCreatedAt()
        );
    }
}
