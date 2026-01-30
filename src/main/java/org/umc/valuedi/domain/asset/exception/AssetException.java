package org.umc.valuedi.domain.asset.exception;

import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class AssetException extends GeneralException {
    public AssetException(BaseErrorCode code) {
        super(code);
    }
}
