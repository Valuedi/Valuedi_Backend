package org.umc.valuedi.domain.connection.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ConnectionErrorCode implements BaseErrorCode {

    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONNECTION404_1", "해당 연동 정보를 찾을 수 없습니다."),
    CONNECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CONNECTION403_1", "해당 연동 정보에 접근할 권한이 없습니다."),

    // Sync 관련
    SYNC_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "SYNC404_1", "해당 동기화 로그를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
