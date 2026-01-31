package org.umc.valuedi.domain.savings.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class SavingsException extends GeneralException {
    public SavingsException(BaseErrorCode code) {
        super(code);
    }
}
