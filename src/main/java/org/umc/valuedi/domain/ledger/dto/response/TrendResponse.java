package org.umc.valuedi.domain.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendResponse {
    private YearMonth yearMonth;
    private Long totalExpense;
}
