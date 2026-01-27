package org.umc.valuedi.domain.goal.validator;

import org.umc.valuedi.domain.goal.constant.GoalStyleCatalog;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;

import java.time.LocalDate;

public final class GoalValidator {

    private GoalValidator() {}

    public static void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        if (start.isAfter(end)) {
            throw new GoalException(GoalErrorCode.INVALID_DATE_RANGE);
        }
    }

    public static void validateStyle(String colorCode, Integer iconId) {
        String normalized = GoalStyleCatalog.normalizeColor(colorCode);

        if (!GoalStyleCatalog.isValidColor(normalized)) {
            throw new GoalException(GoalErrorCode.GOAL_COLOR_INVALID);
        }
        if (!GoalStyleCatalog.isValidIcon(iconId)) {
            throw new GoalException(GoalErrorCode.GOAL_ICON_INVALID);
        }
    }
}
