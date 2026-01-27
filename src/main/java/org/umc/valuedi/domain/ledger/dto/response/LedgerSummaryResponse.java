package org.umc.valuedi.domain.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerSummaryResponse {
    private Long totalIncome;
    private Long totalExpense;
    private Long balance;
    private Long diffFromLastMonth;
}
