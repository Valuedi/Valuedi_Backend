package org.umc.valuedi.domain.terms.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum TermsErrorCode implements BaseErrorCode {

    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS404_1", "해당 약관을 찾을 수 없습니다."),
    MANDATORY_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS400_1", "필수 약관에 모두 동의해야 회원가입이 가능합니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
