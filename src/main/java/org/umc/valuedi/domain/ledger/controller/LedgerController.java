package org.umc.valuedi.domain.ledger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.ledger.dto.request.LedgerSyncRequest;
import org.umc.valuedi.domain.ledger.dto.response.*;
import org.umc.valuedi.domain.ledger.enums.LedgerSortType;
import org.umc.valuedi.domain.ledger.exception.code.LedgerSuccessCode;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.ledger.service.query.LedgerQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;
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
    public ApiResponse<LedgerListResponse> getTransactions(@CurrentMember Long memberId, YearMonth yearMonth, LocalDate date, int page, int size, LedgerSortType sort) {
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_LIST_FETCHED, ledgerQueryService.getTransactions(memberId, yearMonth, date, page, size, sort));
    }

    @Override
    public ApiResponse<LedgerSummaryResponse> getMonthlySummary(@CurrentMember Long memberId, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_SUMMARY_FETCHED, ledgerQueryService.getMonthlySummary(memberId, yearMonth));
    }

    @Override
    public ApiResponse<List<CategoryStatResponse>> getCategoryStats(@CurrentMember Long memberId, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.CATEGORY_STATS_FETCHED, ledgerQueryService.getCategoryStats(memberId, yearMonth));
    }

    @Override
    public ApiResponse<List<DailyStatResponse>> getDailyStats(@CurrentMember Long memberId, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.DAILY_STATS_FETCHED, ledgerQueryService.getDailyStats(memberId, yearMonth));
    }

    @Override
    public ApiResponse<List<TrendResponse>> getTrend(@CurrentMember Long memberId, YearMonth fromYearMonth, YearMonth toYearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.TREND_FETCHED, ledgerQueryService.getTrend(memberId, fromYearMonth, toYearMonth));
    }

    @Override
    public ApiResponse<List<TopCategoryResponse>> getTopCategories(@CurrentMember Long memberId, YearMonth yearMonth, int limit) {
        return ApiResponse.onSuccess(LedgerSuccessCode.TOP_CATEGORY_FETCHED, ledgerQueryService.getTopCategories(memberId, yearMonth, limit));
    }

    @Override
    public ApiResponse<PeerCompareResponse> getPeerComparison(@CurrentMember Long memberId, YearMonth yearMonth) {
        return ApiResponse.onSuccess(LedgerSuccessCode.PEER_COMPARE_FETCHED, ledgerQueryService.getPeerComparison(memberId, yearMonth));
    }

    @Override
    public ApiResponse<String> syncTransactions(@CurrentMember Long memberId, LedgerSyncRequest request) {
        ledgerSyncService.syncTransactions(memberId, request);
        return ApiResponse.onSuccess(LedgerSuccessCode.LEDGER_SYNC_SUCCESS, "거래내역 동기화가 완료되었습니다.");
    }
}
