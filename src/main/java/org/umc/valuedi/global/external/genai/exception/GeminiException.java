package org.umc.valuedi.global.external.genai.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;
import org.umc.valuedi.global.external.genai.exception.code.GeminiErrorCode;

public class GeminiException extends GeneralException {

    private final Throwable originalCause;

    public GeminiException(BaseErrorCode code) {
        super(code);
        this.originalCause = null;
    }

    public GeminiException(BaseErrorCode code, Throwable e) {
        super(code);
        this.originalCause = e;
    }

    public Throwable getOriginalCause() {
        return originalCause;
    }

    public GeminiErrorCode getErrorCode() {
        BaseErrorCode code = super.getCode();
        if (code instanceof GeminiErrorCode geminiCode) {
            return geminiCode;
        }
        throw new IllegalStateException("GeminiException code is not GeminiErrorCode: " + code.getClass());
    }
}
