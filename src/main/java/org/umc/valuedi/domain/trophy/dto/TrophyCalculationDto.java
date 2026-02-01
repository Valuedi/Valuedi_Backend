package org.umc.valuedi.domain.trophy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrophyCalculationDto {
    private long totalAmount;
    private long maxAmount;
    private int transactionCount;
    // 필요한 메트릭 추가
}
