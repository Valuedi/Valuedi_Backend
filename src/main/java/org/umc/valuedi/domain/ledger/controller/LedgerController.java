package org.umc.valuedi.domain.ledger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.ledger.dto.request.LedgerSyncRequest;
import org.umc.valuedi.domain.ledger.dto.response.*;
import org.umc.valuedi.domain.ledger.exception.code.LedgerSuccessCode;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.ledger.service.query.LedgerQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LedgerController implements LedgerControllerDocs {

    private final LedgerQueryService ledgerQueryService;
    private final LedgerSyncService ledgerSyncService;

    @Override
    public ApiResponse<LedgerListResponse> getTransactions(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth, LocalDate date, int page, int size, String sort) {
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_LIST_FETCHED, ledgerQueryService.getTransactions(Long.valueOf(userDetails.getUsername()), yearMonth, date, page, size, sort));
    }

    @Override
    public ApiResponse<LedgerSummaryResponse> getMonthlySummary(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_SUMMARY_FETCHED, ledgerQueryService.getMonthlySummary(Long.valueOf(userDetails.getUsername()), yearMonth));
    }

    @Override
    public ApiResponse<List<CategoryStatResponse>> getCategoryStats(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.CATEGORY_STATS_FETCHED, ledgerQueryService.getCategoryStats(Long.valueOf(userDetails.getUsername()), yearMonth));
    }

    @Override
    public ApiResponse<List<DailyStatResponse>> getDailyStats(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.DAILY_STATS_FETCHED, ledgerQueryService.getDailyStats(Long.valueOf(userDetails.getUsername()), yearMonth));
    }

    @Override
    public ApiResponse<List<TrendResponse>> getTrend(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth fromYearMonth, YearMonth toYearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.TREND_FETCHED, ledgerQueryService.getTrend(Long.valueOf(userDetails.getUsername()), fromYearMonth, toYearMonth));
    }

    @Override
    public ApiResponse<List<TopCategoryResponse>> getTopCategories(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth, int limit) {
        return ApiResponse.onSuccess(LedgerSuccessCode.TOP_CATEGORY_FETCHED, ledgerQueryService.getTopCategories(Long.valueOf(userDetails.getUsername()), yearMonth, limit));
    }

    @Override
    public ApiResponse<PeerCompareResponse> getPeerComparison(@AuthenticationPrincipal CustomUserDetails userDetails, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.PEER_COMPARE_FETCHED, ledgerQueryService.getPeerComparison(Long.valueOf(userDetails.getUsername()), yearMonth));
    }

    @Override
    public ApiResponse<String> syncTransactions(@AuthenticationPrincipal CustomUserDetails userDetails, LedgerSyncRequest request) {
        ledgerSyncService.syncTransactions(Long.valueOf(userDetails.getUsername()), request);
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_SYNC_SUCCESS, "거래내역 동기화가 완료되었습니다.");
    }
}
