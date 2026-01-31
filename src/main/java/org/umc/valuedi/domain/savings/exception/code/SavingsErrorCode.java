package org.umc.valuedi.domain.savings.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum SavingsErrorCode implements BaseErrorCode {

    SAVINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVINGS404_1", "해당 적금 상품을 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
