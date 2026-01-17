package org.umc.valuedi.domain.mbti.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum MbtiErrorCode implements BaseErrorCode {

    NO_ACTIVE_QUESTIONS(HttpStatus.NOT_FOUND, "MBTI_001", "활성화된 MBTI 문항이 없습니다."),
    DUPLICATE_QUESTION_ID(HttpStatus.BAD_REQUEST, "MBTI_002", "answers에 중복 questionId가 있습니다."),
    NOT_ALL_ANSWERED(HttpStatus.BAD_REQUEST, "MBTI_003", "모든 문항에 답변해야 합니다."),
    INVALID_QUESTION_ID(HttpStatus.BAD_REQUEST, "MBTI_004", "유효하지 않은 questionId가 포함되어 있습니다."),
    INVALID_CHOICE_VALUE(HttpStatus.BAD_REQUEST, "MBTI_005", "choiceValue는 1~5 사이의 정수여야 합니다."),
    NO_ACTIVE_RESULT(HttpStatus.NOT_FOUND, "MBTI_006", "활성화된 MBTI 결과가 없습니다."),
    TYPE_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "MBTI404_3", "해당 MBTI 유형 정보가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
