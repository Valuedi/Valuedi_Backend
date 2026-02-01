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
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "GOAL404_6", "계좌를 찾을 수 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "GOAL400_1", "시작일은 종료일보다 늦을 수 없습니다."),
    INVALID_GOAL_LIST_STATUS(HttpStatus.BAD_REQUEST, "GOAL400_2", "목표 목록 조회 status는 ACTIVE 또는 COMPLETE만 가능합니다."),
    GOAL_ALREADY_LINKED_ACCOUNT(HttpStatus.BAD_REQUEST, "GOAL400_3", "해당 목표는 이미 계좌가 연결되어 있습니다."),
    ACCOUNT_ALREADY_LINKED_TO_GOAL(HttpStatus.BAD_REQUEST, "GOAL400_4", "해당 계좌는 이미 다른 목표에 연결되어 있습니다."),
    GOAL_FORBIDDEN(HttpStatus.FORBIDDEN, "GOAL403_1", "해당 목표에 대한 권한이 없습니다."),
    GOAL_ACCOUNT_INACTIVE(HttpStatus.GONE, "GOAL_410_1", "연결된 계좌가 비활성화(삭제)되어 목표 상세를 조회할 수 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
