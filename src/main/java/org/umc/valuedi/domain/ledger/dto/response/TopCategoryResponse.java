package org.umc.valuedi.domain.ledger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCategoryResponse {
    private String categoryName;
    private Long totalAmount;
    private int rank;
}
