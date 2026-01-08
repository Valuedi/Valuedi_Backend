package org.umc.valuedi.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "MEMBER409_1", "이미 사용 중인 아이디입니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
