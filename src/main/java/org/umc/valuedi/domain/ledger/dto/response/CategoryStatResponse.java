package org.umc.valuedi.domain.ledger.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatResponse {
    private String categoryCode;
    private String categoryName;
    private Long totalAmount;
    @Setter
    private Double percentage;

    // QueryDSL Projection Constructor
    public CategoryStatResponse(String categoryCode, String categoryName, Long totalAmount) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }
}