package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSyncProcessor {

    private final AssetFetchService assetFetchService;
    private final LedgerSyncService ledgerSyncService;

    /**
     * 실제 동기화 로직을 수행하는 비동기 메서드
     */
    @Async("assetFetchExecutor")
    public void runSyncProcess(Member member) {
        log.info("자산 동기화 백그라운드 작업을 시작합니다. 회원 ID: {}", member.getId());
        try {
            // 트랜잭션 1: 데이터 수집 및 저장
            AssetResDTO.AssetSyncResult syncResult = assetFetchService.fetchAndSaveLatestData(member);

            // 새로 수집된 데이터가 있을 경우에만 가계부 동기화 로직 수행
            boolean hasNewData = syncResult.getNewBankTransactionCount() > 0 || syncResult.getNewCardApprovalCount() > 0;
            if (hasNewData) {
                LocalDate fromDate = syncResult.getFromDate();
                LocalDate toDate = syncResult.getToDate();
                
                // 트랜잭션 2: 가계부 연동 및 최종 업데이트
                ledgerSyncService.syncTransactionsAndUpdateMember(member, fromDate, toDate);
            } else {
                // 새로 수집된 데이터가 없어도 동기화 시간은 갱신
                ledgerSyncService.updateMemberLastSyncedAt(member);
            }
            log.info("자산 동기화 백그라운드 작업을 성공적으로 완료했습니다. 회원 ID: {}", member.getId());
        } catch (Exception e) {
            // 비동기 작업 내에서 발생하는 모든 예외를 로깅
            log.error("자산 동기화 백그라운드 작업 중 오류 발생. 회원 ID: {}", member.getId(), e);
        }
    }
}
