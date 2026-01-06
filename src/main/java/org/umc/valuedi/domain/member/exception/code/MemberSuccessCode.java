package org.umc.valuedi.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    USERNAME_CHECK_OK(HttpStatus.OK, "MEMBER200_1", "사용 가능한 아이디입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
