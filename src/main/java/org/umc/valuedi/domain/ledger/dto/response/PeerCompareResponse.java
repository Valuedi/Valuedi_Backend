package org.umc.valuedi.domain.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerCompareResponse {
    private Long myTotalExpense;
    private Long perAverageExpense;
    private String message;
}
