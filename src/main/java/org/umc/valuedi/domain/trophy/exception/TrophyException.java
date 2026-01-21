package org.umc.valuedi.domain.trophy.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class TrophyException extends GeneralException {
    public TrophyException(BaseErrorCode code) {
        super(code);
    }
}
