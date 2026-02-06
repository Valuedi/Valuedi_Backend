package org.umc.valuedi.domain.asset.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.exception.code.AssetErrorCode;
import org.umc.valuedi.domain.connection.enums.SyncType;
import org.umc.valuedi.domain.connection.service.command.SyncLogCommandService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetSyncFacadeService {

    private final MemberRepository memberRepository;
    private final AssetSyncProcessor assetSyncProcessor;
    private final SyncLogCommandService syncLogCommandService;

    private static final long SYNC_COOL_DOWN_MINUTES = 10;

    /**
     * 동기화 요청을 접수하고, 실제 작업은 백그라운드로 넘깁니다.
     */
    public void refreshAssetSync(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        validateSyncCoolDown(member);

        // 동기화 로그 시작
        Long logId = syncLogCommandService.startSyncLog(memberId, SyncType.ALL);

        // 실제 동기화 프로세스를 비동기적으로 호출
        assetSyncProcessor.runSyncProcess(member.getId(), logId);
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
