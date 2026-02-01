package org.umc.valuedi.domain.goal.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@RequiredArgsConstructor
public enum GoalSuccessCode implements BaseSuccessCode {

    GOAL_CREATED(HttpStatus.CREATED, "GOAL201_1", "목표가 성공적으로 생성되었습니다."),
    GOAL_LIST_FETCHED(HttpStatus.OK, "GOAL200_1", "목표 목록 조회 성공"),
    GOAL_DETAIL_FETCHED(HttpStatus.OK, "GOAL200_2", "목표 상세 조회 성공"),
    GOAL_UPDATED(HttpStatus.OK, "GOAL200_3", "목표가 성공적으로 수정되었습니다."),
    GOAL_DELETED(HttpStatus.OK, "GOAL204_1", "목표가 성공적으로 삭제되었습니다."),
    GOAL_ACTIVE_COUNT_FETCHED(HttpStatus.OK, "GOAL200_5", "진행 중인 목표 개수 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}