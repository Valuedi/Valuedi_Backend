package org.umc.valuedi.domain.goal.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements BaseErrorCode {

    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "GOAL404_1", "목표를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "회원을 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "GOAL400_1", "시작일은 종료일보다 늦을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
