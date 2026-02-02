package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.exception.code.AssetErrorCode;
import org.umc.valuedi.domain.ledger.service.command.LedgerSyncService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetSyncFacadeService {

    private final MemberRepository memberRepository;
    private final AssetFetchService assetFetchService;
    private final LedgerSyncService ledgerSyncService;

    private static final long SYNC_COOL_DOWN_MINUTES = 10;

    public AssetResDTO.AssetSyncRefreshResponse refreshAssetSync(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException((MemberErrorCode.MEMBER_NOT_FOUND)));

        validateSyncCoolDown(member);

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

        return AssetResDTO.AssetSyncRefreshResponse.builder()
                .newBankTransactionCount(syncResult.getNewBankTransactionCount())
                .newCardApprovalCount(syncResult.getNewCardApprovalCount())
                .successOrganizations(syncResult.getSuccessOrganizations())
                .failureOrganizations(syncResult.getFailureOrganizations())
                .build();
    }

    private void validateSyncCoolDown(Member member) {
        if (member.getLastSyncedAt() == null) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(member.getLastSyncedAt(), now);

        if (duration.toMinutes() < SYNC_COOL_DOWN_MINUTES) {
            throw new AssetException(AssetErrorCode.SYNC_COOL_DOWN);
        }
    }
}
