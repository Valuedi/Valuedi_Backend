package org.umc.valuedi.domain.mbti.dto;

import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.enums.MbtiQuestionCategory;

public record MbtiQuestionResponseDto(
        Long id,
        MbtiQuestionCategory category,
        String content
) {
    public static MbtiQuestionResponseDto from(MbtiQuestion q) {
        return new MbtiQuestionResponseDto(q.getId(), q.getCategory(), q.getContent());
    }
}
