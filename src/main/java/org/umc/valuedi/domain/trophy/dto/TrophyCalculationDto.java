package org.umc.valuedi.domain.trophy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrophyCalculationDto {
    private long totalAmount;
    private long maxAmount;
    private int transactionCount;
    private int hour; // 결제 시간
    private String categoryCode; // 카테고리
}
