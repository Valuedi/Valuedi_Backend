package org.umc.valuedi.domain.ledger.dto.response;

import lombok.*;
import org.umc.valuedi.domain.ledger.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerListResponse {
    private List<LedgerDetail> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LedgerDetail {
        private Long id;
        private String title;
        private Long amount;
        private TransactionType type; // INCOME, EXPENSE, TRANSFER, REFUND
        private String categoryCode;
        private String categoryName;
        private LocalDateTime transactionAt;
        private String memo;
    }
}