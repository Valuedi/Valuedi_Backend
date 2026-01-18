package org.umc.valuedi.domain.asset.connection.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ConnectionErrorCode implements BaseErrorCode {

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
