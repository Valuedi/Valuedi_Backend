package org.umc.valuedi.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // Kakao
    REQUIRED_INFO_MISSING(HttpStatus.BAD_REQUEST, "AUTH400_1", "필수 제공 정보(이름, 성별, 생일)가 누락되었습니다."),

    // Access Token & Refresh Token
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_2", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_3", "만료된 토큰입니다."),
    NOT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_4", "엑세스 토큰이 아닙니다."),

    // State
    INVALID_STATE(HttpStatus.UNAUTHORIZED, "AUTH401_5", "보안 인증 값(state)이 일치하지 않거나 만료되었습니다."),

    // username
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH409_1", "이미 사용 중인 아이디입니다."),

    // email
    EMAIL_ALREADY_SENT(HttpStatus.TOO_MANY_REQUESTS, "AUTH429_1", "이미 인증번호가 발송되었습니다. 1분 후 다시 시도해 주세요."),
    EMAIL_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH404_1", "인증번호가 만료되었거나 존재하지 않습니다."),
    EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH400_2", "인증번호가 일치하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
