package org.umc.valuedi.domain.trophy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.trophy.dto.TrophyCalculationDto;

import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum TrophyType {
    // 카페/간식 카테고리(CAFE_SNACK) + 4500원 이상
    COFFEE_ADDICT("커피중독자", stats ->
            "CAFE_SNACK".equals(stats.getCategoryCode()) && stats.getTotalAmount() >= 4500),

    // 식비 카테고리(FOOD) + 21시 이후 + 50,000원 이상
    LATE_NIGHT_SNACK("야식의 왕", stats -> stats.getHour() >= 21 && stats.getTotalAmount() >= 50000),

    NO_SPEND_DAY("무지출데이", stats -> stats.getTotalAmount() <= 500),
    MIN_SPEND("최소소비", stats -> stats.getTotalAmount() <= 4500),
    MAX_SPEND("최다소비", stats -> stats.getTotalAmount() >= 50000)

    ;

    private final String displayName;
    private final Predicate<TrophyCalculationDto> condition;

    public boolean isAchieved(TrophyCalculationDto stats) {
        return condition.test(stats);
    }
}
