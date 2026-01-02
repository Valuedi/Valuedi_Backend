package org.umc.valuedi.global.apiPayload.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private final BaseErrorCode code;
}