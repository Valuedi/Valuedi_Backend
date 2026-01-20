package org.umc.valuedi.domain.terms.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum TermsSuccessCode implements BaseSuccessCode {

    TERMS_AGREE_SUCCESS(HttpStatus.OK, "TERMS200_1", "약관 동의가 저장되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
