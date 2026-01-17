package org.umc.valuedi.domain.mbti.exception;

import lombok.Getter;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;

@Getter
public class MbtiException extends RuntimeException {

    private final MbtiErrorCode errorCode;

    public MbtiException(MbtiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MbtiException(MbtiErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
