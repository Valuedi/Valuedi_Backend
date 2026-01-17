package org.umc.valuedi.domain.mbti.dto;

import org.umc.valuedi.domain.mbti.enums.MbtiType;
import java.util.List;

public record FinanceMbtiTypeInfoDto(
        MbtiType type,                 // 영문
        String title,                  // 별명
        String tagline,                // 1줄 설명
        String detail,                 // 상세설명
        List<String> cautions,         // 주의할 점
        List<String> recommendedActions // 추천 행동
) {}
