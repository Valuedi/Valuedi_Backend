package org.umc.valuedi.domain.ledger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.ledger.enums.LedgerSortType;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.domain.ledger.dto.request.LedgerSyncRequest;
import org.umc.valuedi.domain.ledger.dto.response.*;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Ledger (거래내역)", description = "가계부 거래내역 조회 및 동기화 API")
public interface LedgerControllerDocs {

    @Operation(summary = "거래 내역 조회", description = "월별/일별 거래 내역을 페이징하여 조회합니다.")
    @GetMapping("/api/transactions")
    ApiResponse<LedgerListResponse> getTransactions(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth,
            @Parameter(description = "특정 일자 필터 (YYYY-MM-DD)") @RequestParam(required = false) LocalDate date,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 (LATEST, OLDEST, AMOUNT_DESC, AMOUNT_ASC)") @RequestParam(defaultValue = "LATEST") LedgerSortType sort
    );

    @Operation(summary = "월 소비 내역 요약", description = "이번 달 총 수입/지출 및 전월 대비 증감액을 조회합니다.")
    @GetMapping("/api/transactions/summary")
    ApiResponse<LedgerSummaryResponse> getMonthlySummary(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth
    );

    @Operation(summary = "카테고리별 소비 집계", description = "월간 카테고리별 지출 합계와 비율을 조회합니다.")
    @GetMapping("/api/transactions/by-category")
    ApiResponse<List<CategoryStatResponse>> getCategoryStats(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth
    );

    @Operation(summary = "일별 수입/지출 합계 (달력)", description = "달력에 표시할 일별 수입/지출 총액을 조회합니다.")
    @GetMapping("/api/transactions/by-day")
    ApiResponse<List<DailyStatResponse>> getDailyStats(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth
    );

    @Operation(summary = "월별 지출 추이", description = "지정된 기간 동안의 월별 지출 총액 추이를 조회합니다.")
    @GetMapping("/api/transactions/trend")
    ApiResponse<List<TrendResponse>> getTrend(
            @CurrentMember Long memberId,
            @Parameter(description = "시작 년월 (YYYY-MM)", required = true) @RequestParam YearMonth fromYearMonth,
            @Parameter(description = "종료 년월 (YYYY-MM)", required = true) @RequestParam YearMonth toYearMonth
    );

    @Operation(summary = "최다 소비 항목 조회", description = "해당 월에 가장 많이 소비한 카테고리/항목을 조회합니다.")
    @GetMapping("/api/transactions/top-category")
    ApiResponse<List<TopCategoryResponse>> getTopCategories(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth,
            @Parameter(description = "조회 개수") @RequestParam(defaultValue = "3") int limit
    );

    @Operation(summary = "또래 비교 (MVP)", description = "나와 비슷한 또래의 평균 지출과 비교합니다. (MVP: 더미 데이터)")
    @GetMapping("/api/transactions/peer-compare")
    ApiResponse<PeerCompareResponse> getPeerComparison(
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", required = true) @RequestParam YearMonth yearMonth
    );

    @Operation(summary = "[개발용] 거래내역 동기화", description = "금융사 연동 API로 통합되어 실제 서비스에서는 사용되지 않습니다. 추후 테스트 후 삭제 예정입니다.")
    @PostMapping("/api/transactions/sync")
    ApiResponse<String> syncTransactions(
            @CurrentMember Long memberId,
            @RequestBody LedgerSyncRequest request
    );

    @Operation(summary = "카테고리 재매칭", description = "지정된 기간의 거래내역 카테고리를 다시 분류합니다. (사용자 수정 내역 제외)")
    @PostMapping("/api/transactions/rematch")
    ApiResponse<String> rematchCategories(
            @CurrentMember Long memberId,
            @RequestBody LedgerSyncRequest request
    );
}
