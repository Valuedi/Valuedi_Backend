package org.umc.valuedi.domain.connection.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class ConnectionException extends GeneralException {
    public ConnectionException(BaseErrorCode code) {
        super(code);
    }
}
