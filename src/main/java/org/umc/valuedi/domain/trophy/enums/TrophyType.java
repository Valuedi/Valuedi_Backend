package org.umc.valuedi.domain.trophy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.trophy.dto.TrophyCalculationDto;

import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum TrophyType {
    COFFEE_ADDICT("카페인 중독", stats -> stats.getTotalAmount() >= 4500),
    LATE_NIGHT_SNACK("야식 매니아", stats -> stats.getTotalAmount() >= 50000),
    NO_SPEND_DAY("무지출 챌린지", stats -> stats.getTotalAmount() <= 500);

    private final String displayName;
    private final Predicate<TrophyCalculationDto> condition;

    public boolean isAchieved(TrophyCalculationDto stats) {
        return condition.test(stats);
    }
}
