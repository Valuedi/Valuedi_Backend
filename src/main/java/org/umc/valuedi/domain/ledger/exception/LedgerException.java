package org.umc.valuedi.domain.ledger.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class LedgerException extends GeneralException {
    public LedgerException(BaseErrorCode code) {
        super(code);
    }
}
