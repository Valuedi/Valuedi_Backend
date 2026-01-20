package org.umc.valuedi.domain.terms.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class TermsException extends GeneralException {
    public TermsException(BaseErrorCode code) {
        super(code);
    }
}
