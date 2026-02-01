package org.umc.valuedi.domain.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatResponse {
    private LocalDate date;
    private Long totalIncome;
    private Long totalExpense;
}
