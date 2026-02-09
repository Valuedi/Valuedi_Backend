package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.connection.service.command.SyncLogCommandService;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetSyncProcessor {

    private final AssetFetchService assetFetchService;
    private final LedgerSyncService ledgerSyncService;
    private final SyncLogCommandService syncLogCommandService;
    private final MemberRepository memberRepository;

    /**
     * 실제 동기화 로직을 수행하는 비동기 메서드
     */
    @Async("assetFetchExecutor")
    @Transactional
    public void runSyncProcess(Long memberId, Long logId) {
        log.info("자산 동기화 백그라운드 작업을 시작합니다. 회원 ID: {}", memberId);
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
            // 트랜잭션 1: 데이터 수집 및 저장
            AssetResDTO.AssetSyncResult syncResult = assetFetchService.fetchAndSaveLatestData(member);

            // 새로 수집된 데이터가 있을 경우에만 가계부 동기화 로직 수행
            boolean hasNewData = syncResult.getNewBankTransactionCount() > 0 || syncResult.getNewCardApprovalCount() > 0;
            if (hasNewData) {
                LocalDate fromDate = syncResult.getFromDate();
                LocalDate toDate = syncResult.getToDate();
                
                // 트랜잭션 2: 가계부 연동
                ledgerSyncService.syncTransactions(member, fromDate, toDate);
            }

            // 트랜잭션 3: 동기화 로그 및 최종 시간 업데이트
            member.updateLastSyncedAt();
            syncLogCommandService.updateToSuccess(logId);
            log.info("자산 동기화 백그라운드 작업을 성공적으로 완료했습니다. 회원 ID: {}", member.getId());

        } catch (Exception e) {
            // 실패 로그 기록
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : "Unknown Error";
            syncLogCommandService.updateToFailed(logId, errorMessage);
            log.error("자산 동기화 백그라운드 작업 중 오류 발생. 회원 ID: {}", memberId, e);
        }
    }
}
