package org.umc.valuedi.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    LOGIN_OK(HttpStatus.OK, "AUTH200_1", "로그인에 성공했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
