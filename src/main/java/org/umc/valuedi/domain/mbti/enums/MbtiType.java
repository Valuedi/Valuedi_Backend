package org.umc.valuedi.domain.mbti.enums;

import lombok.Getter;

@Getter
public enum MbtiType {
    // 코드 규칙: [A/S][I/P][G/C][V/R]
    // Anxiety / Stability
    // Impulse / Planning
    // Aggressive / Conservative
    // Avoidance / Rational

    AIGV, AIGR, AICV, AICR,
    APGV, APGR, APCV, APCR,
    SIGV, SIGR, SICV, SICR,
    SPGV, SPGR, SPCV, SPCR;

    public static MbtiType fromScores(
            int anxiety, int stability,
            int impulse, int planning,
            int aggressive, int conservative,
            int avoidance, int reasoning
    ) {
        char d1 = (anxiety >= stability) ? 'A' : 'S';
        char d2 = (impulse >= planning) ? 'I' : 'P';
        char d3 = (aggressive >= conservative) ? 'G' : 'C';
        char d4 = (avoidance >= reasoning) ? 'V' : 'R';

        String code = "" + d1 + d2 + d3 + d4;
        return MbtiType.valueOf(code);
    }
}
