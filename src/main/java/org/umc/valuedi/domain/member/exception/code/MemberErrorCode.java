package org.umc.valuedi.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER403_1", "휴면 상태의 회원입니다."),
    MEMBER_DELETED(HttpStatus.FORBIDDEN, "MEMBER403_2", "탈퇴한 회원입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
