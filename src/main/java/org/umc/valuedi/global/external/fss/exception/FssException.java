package org.umc.valuedi.global.external.fss.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class FssException extends GeneralException {
    public FssException(BaseErrorCode code) {
        super(code);
    }
}
