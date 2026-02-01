package org.umc.valuedi.domain.member.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    MEMBER_INFO_GET_SUCCESS(HttpStatus.OK, "MEMBER200_1", "회원 기본 정보 조회에 성공했습니다."),
    WITHDRAWAL_SUCCESS(HttpStatus.OK, "MEMBER200_2", "회원 탈퇴가 완료되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
