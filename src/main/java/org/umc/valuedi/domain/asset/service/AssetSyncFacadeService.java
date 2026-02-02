package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public AssetResDTO.AssetSyncRefreshResponse refreshAssetSync(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException((MemberErrorCode.MEMBER_NOT_FOUND)));

        validateSyncCoolDown(member);

        AssetResDTO.AssetSyncResult syncResult = assetFetchService.fetchAndSaveLatestData(member);

        // 새로 수집된 데이터가 있을 경우에만 가계부 동기화 로직 수행
        boolean hasNewData = syncResult.getNewBankTransactionCount() > 0 || syncResult.getNewCardApprovalCount() > 0;
        if (hasNewData) {
            LocalDate fromDate = syncResult.getFromDate();
            LocalDate toDate = syncResult.getToDate();
            ledgerSyncService.syncTransactions(member, fromDate, toDate);
        }
        
        member.updateLastSyncedAt();

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
