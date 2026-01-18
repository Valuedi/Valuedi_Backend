package org.umc.valuedi.global.external.codef.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class CodefException extends GeneralException {
    public CodefException(BaseErrorCode code) {
        super(code);
    }
}
