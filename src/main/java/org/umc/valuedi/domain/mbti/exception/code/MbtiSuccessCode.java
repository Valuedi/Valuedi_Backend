package org.umc.valuedi.domain.mbti.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum MbtiSuccessCode implements BaseSuccessCode {

    MBTI_TEST_SUBMITTED(HttpStatus.CREATED, "201_1", "금융 MBTI 테스트 제출에 성공했습니다."),
    MBTI_QUESTIONS_FETCHED(HttpStatus.OK, "GOAL200_1", "금융 MBTI 문항 조회에 성공했습니다."),
    MBTI_RESULT_FETCHED(HttpStatus.OK, "GOAL200_2", "금융 MBTI 결과 조회에 성공했습니다."),
    MBTI_TYPE_LIST_FETCHED(HttpStatus.OK, "GOAL200_3", "금융 MBTI 유형 목록 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}