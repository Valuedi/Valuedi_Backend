package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;


@Getter
@AllArgsConstructor
public enum AssetSuccessCode implements BaseSuccessCode {

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
