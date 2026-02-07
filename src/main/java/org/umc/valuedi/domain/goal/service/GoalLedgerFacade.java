package org.umc.valuedi.domain.goal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.service.command.AssetFetchService;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.service.query.GoalLedgerQueryService;
import org.umc.valuedi.domain.ledger.dto.response.LedgerListResponse;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalLedgerFacade {

    private final GoalRepository goalRepository;
    private final AssetFetchService assetFetchService;
    private final LedgerSyncService ledgerSyncService;
    private final GoalLedgerQueryService goalLedgerQueryService;

    /**
     * 목표 거래내역 조회 (동기화 포함)
     * 트랜잭션을 분리하여 최신 데이터를 조회할 수 있도록 함
     */
    public LedgerListResponse getGoalLedgerTransactions(Long memberId, Long goalId, int page, int size) {
        // 1. 목표 정보 조회 (검증용)
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        if (!goal.getMember().getId().equals(memberId)) {
            throw new GoalException(GoalErrorCode.GOAL_FORBIDDEN);
        }

        // 2. 자산 및 가계부 동기화 (각각 별도의 트랜잭션으로 실행됨)
        try {
            // 자산 동기화 (REQUIRES_NEW)
            // 여기서 최신 거래내역을 DB에 저장함
            assetFetchService.fetchAndSaveLatestData(goal.getMember());

            // 가계부 동기화 (REQUIRES_NEW)
            // 목표 기간 내의 데이터가 누락되지 않도록, 목표 시작일 ~ 오늘(또는 종료일)까지 동기화 수행
            LocalDate fromDate = goal.getStartDate();
            LocalDate toDate = LocalDate.now();
            
            // 목표 종료일이 오늘보다 과거라면 종료일까지, 아니면 오늘까지
            if (goal.getEndDate().isBefore(toDate)) {
                toDate = goal.getEndDate();
            }
            
            // 시작일이 종료일보다 늦으면(미래 목표 등) 동기화 수행 안 함
            if (!fromDate.isAfter(toDate)) {
                ledgerSyncService.syncTransactionsAndUpdateMember(memberId, fromDate, toDate);
            }

        } catch (Exception e) {
            log.warn("목표 거래내역 조회 중 동기화 실패 (기존 데이터로 조회): {}", e.getMessage());
        }

        // 3. 최종 조회 (새로운 트랜잭션)
        return goalLedgerQueryService.getGoalLedgerTransactions(memberId, goalId, page, size);
    }
}
