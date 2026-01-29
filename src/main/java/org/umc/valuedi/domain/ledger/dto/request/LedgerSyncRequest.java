package org.umc.valuedi.domain.ledger.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;

@Getter
@NoArgsConstructor
public class LedgerSyncRequest {
    @Schema(description = "동기화할 년월 (YYYY-MM)", example = "2026-01")
    private YearMonth yearMonth;
    @Schema(description = "동기화 시작일", example = "2026-01-01")
    private LocalDate fromDate;
    @Schema(description = "동기화 종료일", example = "2026-01-31")
    private LocalDate toDate;
}
