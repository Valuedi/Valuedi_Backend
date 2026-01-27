package org.umc.valuedi.domain.ledger.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.ledger.converter.LedgerConverter;
import org.umc.valuedi.domain.ledger.dto.response.*;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.repository.LedgerQueryRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerQueryService {

    private final LedgerQueryRepository ledgerQueryRepository;

    // 거래 내역 목록 조회
    public LedgerListResponse getTransactions(Long memberId, YearMonth yearMonth, LocalDate date, int page, int size, String sort) {
        Page<LedgerEntry> result = ledgerQueryRepository.searchTransactions(memberId, yearMonth, date, PageRequest.of(page, size));
        return LedgerConverter.toLedgerListResponse(result.getContent(), page, size, result.getTotalElements());
    }

    // 월 소비 내역 요약
    public LedgerSummaryResponse getMonthlySummary(Long memberId, YearMonth yearMonth) {
        // 이번 달 수입/지출
        Long totalIncome = ledgerQueryRepository.findTotalIncome(memberId, yearMonth);
        Long totalExpense = ledgerQueryRepository.findTotalExpense(memberId, yearMonth);

        // 지난 달 지출 (비교용)
        Long prevMonthExpense = ledgerQueryRepository.findTotalExpense(memberId, yearMonth.minusMonths(1));

        return LedgerConverter.toLedgerSummaryResponse(totalIncome, totalExpense, prevMonthExpense);
    }

    // 카테고리별 소비 집계
    public List<CategoryStatResponse> getCategoryStats(Long memberId, YearMonth yearMonth) {
        Long totalExpense = ledgerQueryRepository.findTotalExpense(memberId, yearMonth);
        List<CategoryStatResponse> stats = ledgerQueryRepository.findCategoryStats(memberId, yearMonth);

        if (totalExpense > 0) {
            stats.forEach(s -> s.setPercentage((double) s.getTotalAmount() / totalExpense * 100));
        } else {
            stats.forEach(s -> s.setPercentage(0.0));
        }
        return stats;
    }

    // 일별 수입/지출 합계 (달력)
    public List<DailyStatResponse> getDailyStats(Long memberId, YearMonth yearMonth) {
        return ledgerQueryRepository.getDailyStats(memberId, yearMonth);
    }

    // 월별 지출 추이
    public List<TrendResponse> getTrend(Long memberId, YearMonth fromYearMonth, YearMonth toYearMonth){
        // from ~ to 기간 동안의 월별 지출 합계를 조회
        return ledgerQueryRepository.findTrend(memberId, fromYearMonth, toYearMonth);
    }

    // 최다 소비 항목 조회
    public List<TopCategoryResponse> getTopCategories(Long memberId, YearMonth yearMonth, int limit) {
        List<CategoryStatResponse> stats = ledgerQueryRepository.findCategoryStats(memberId, yearMonth);

        // 금액 내림차순 정렬 후 상위 limit개 추출 및 변환
        return stats.stream()
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .limit(limit)
                .map(stat -> TopCategoryResponse.builder()
                        .categoryName(stat.getCategoryName())
                        .totalAmount(stat.getTotalAmount())
                        .rank(stats.indexOf(stat) + 1)
                        .build())
                .collect(Collectors.toList());
    }

    // 7. 또래 비교 (MVP: 더미 데이터)
    public PeerCompareResponse getPeerComparison(Long memberId, YearMonth yearMonth) {
        Long myTotalExpense = ledgerQueryRepository.findTotalExpense(memberId, yearMonth);

        // MVP: 고정된 또래 평균 금액 (예: 120만원)
        long peerAverage = 1200000L;
        long diff = myTotalExpense - peerAverage;

        String message;
        if (diff > 0) {
            message = String.format("또래보다 %,d원 더 썼어요!", diff);
        } else if (diff < 0) {
            message = String.format("또래보다 %,d원 덜 썼어요!", Math.abs(diff));
        } else {
            message = "또래와 비슷하게 썼어요!";
        }

        return PeerCompareResponse.builder()
                .myTotalExpense(myTotalExpense)
                .perAverageExpense(peerAverage)
                .message(message)
                .build();
    }
}
