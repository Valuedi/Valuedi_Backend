package org.umc.valuedi.domain.mbti.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MbtiErrorCode {

    NO_ACTIVE_QUESTIONS("MBTI_001", "활성화된 MBTI 문항이 없습니다."),
    DUPLICATE_QUESTION_ID("MBTI_002", "answers에 중복 questionId가 있습니다."),
    NOT_ALL_ANSWERED("MBTI_003", "모든 문항에 답변해야 합니다."),
    INVALID_QUESTION_ID("MBTI_004", "유효하지 않은 questionId가 포함되어 있습니다."),
    INVALID_CHOICE_VALUE("MBTI_005", "choiceValue는 1~5 사이의 정수여야 합니다."),
    NO_ACTIVE_RESULT("MBTI_006", "활성화된 MBTI 결과가 없습니다.");

    private final String code;
    private final String message;
}
