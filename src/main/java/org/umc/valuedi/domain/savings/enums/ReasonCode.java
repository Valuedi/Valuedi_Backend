package org.umc.valuedi.domain.savings.enums;

public enum ReasonCode {

    HIGH_RATE,        // 금리가 높음
    MATCH_TERM,       // 선호 기간과 일치
    MATCH_RSRV_TYPE,  // 적립유형 일치 (정액/자유)
    STABILITY,        // 안정성 성향
    IMPULSE_GUARD,    // 충동 억제/규칙 기반 선호
    GOAL_FIT,         // 목표/성향 적합(총평)
    OTHER;

    public static ReasonCode from(String raw) {
        if (raw == null || raw.isBlank()) return OTHER;
        try {
            return ReasonCode.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return OTHER; // 모르는 값이면 ETC로
        }
    }
}
