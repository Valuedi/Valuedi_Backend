package org.umc.valuedi.domain.auth.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    KAKAO_AUTH_URL_SUCCESS(HttpStatus.OK, "AUTH200_1", "카카오 로그인 URL이 성공적으로 생성되었습니다."),
    LOGIN_OK(HttpStatus.OK, "AUTH200_2", "로그인에 성공했습니다."),
    USERNAME_AVAILABLE(HttpStatus.OK, "AUTH200_3", "사용 가능한 아이디입니다."),
    EMAIL_SEND_SUCCESS(HttpStatus.OK, "AUTH200_4", "인증번호 발송 요청이 접수되었습니다."),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "AUTH200_5", "이메일 인증에 성공했습니다."),
    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH201_1", "회원가입이 성공적으로 완료되었습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "AUTH200_6", "토큰 재발급에 성공했습니다."),
    LOGOUT_OK(HttpStatus.OK, "AUTH200_7", "로그아웃이 완료되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
