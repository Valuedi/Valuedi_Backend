package org.umc.valuedi.domain.goal.exception;

import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;

public class GoalException extends GeneralException {

    public GoalException(GoalErrorCode errorCode) {
        super(errorCode);
    }
}
