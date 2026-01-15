package org.umc.valuedi.domain.mbti.dto;

import org.umc.valuedi.domain.mbti.enums.MbtiType;
import java.util.List;

public record FinanceMbtiTypeInfoDto(
        MbtiType type,
        String title,              // MBTI
        String shortDescription,    // 2줄 요약
        List<String> cautions,      // 주의할 점 (리스트)
        List<String> recommendedActions // 추천 행동/습관 (리스트)
) {}
