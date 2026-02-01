package org.umc.valuedi.domain.connection.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum ConnectionSuccessCode implements BaseSuccessCode {

    CONNECTION_SUCCESS(HttpStatus.OK, "CODEF200_1", "금융사 계정 연동에 성공하였습니다."),
    CONNECTION_LIST_FETCH_SUCCESS(HttpStatus.OK, "CODEF200_2", "연동 목록 조회에 성공하였습니다."),
    CONNECTION_DELETE_SUCCESS(HttpStatus.OK, "CONNECTION200_1", "성공적으로 금융사 연동이 삭제되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
