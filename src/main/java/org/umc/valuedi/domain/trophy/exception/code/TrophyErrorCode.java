package org.umc.valuedi.domain.trophy.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum TrophyErrorCode implements BaseErrorCode {

    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER403_1", "휴면 상태의 회원입니다."),
    MEMBER_DELETED(HttpStatus.FORBIDDEN, "MEMBER403_2", "탈퇴한 회원입니다."),

    TROPHY_NOT_FOUND(HttpStatus.NOT_FOUND, "TROPHY404_1", "해당 트로피를 찾을 수 없습니다."),
    INVALID_PERIOD_TYPE(HttpStatus.BAD_REQUEST, "TROPHY400_1", "잘못된 기간 타입입니다."),
    INVALID_PERIOD_KEY_FORMAT(HttpStatus.BAD_REQUEST, "TROPHY400_2", "잘못된 날짜 형식입니다. (YYYY-MM 또는 YYYY-MM-DD)"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "해당 회원을 찾을 수 없습니다."), // Member 관련 에러가 없다면 여기서 정의하거나 MemberErrorCode 사용
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
