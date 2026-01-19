package org.umc.valuedi.domain.mbti.exception;

import lombok.Getter;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class MbtiException extends GeneralException {

    public MbtiException(MbtiErrorCode errorCode) {
        super(errorCode);
    }
}
