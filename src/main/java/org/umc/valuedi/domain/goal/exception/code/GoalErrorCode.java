package org.umc.valuedi.domain.goal.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements BaseErrorCode {

    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "GOAL404_1", "목표를 찾을 수 없습니다."),
    GOAL_NOT_EDITABLE(HttpStatus.NOT_FOUND, "GOAL404_2", "완료/취소된 목표는 수정할 수 없습니다."),
    GOAL_STATUS_INVALID(HttpStatus.NOT_FOUND, "GOAL404_3", "이미 완료되었거나 실패 처리된 목표입니다."),
    GOAL_COLOR_INVALID(HttpStatus.NOT_FOUND, "GOAL404_4", "존재하지 않는 목표 색상입니다."),
    GOAL_ICON_INVALID(HttpStatus.NOT_FOUND, "GOAL404_5", "존재하지 않는 목표 아이콘입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "GOAL400_1", "시작일은 종료일보다 늦을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
