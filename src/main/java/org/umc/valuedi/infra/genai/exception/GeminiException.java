package org.umc.valuedi.infra.genai.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class GeminiException extends GeneralException {
    public GeminiException(BaseErrorCode code) {
        super(code);
    }
}
