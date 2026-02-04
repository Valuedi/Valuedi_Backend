package org.umc.valuedi.domain.connection.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.converter.SyncLogConverter;
import org.umc.valuedi.domain.connection.dto.res.SyncLogResDTO;
import org.umc.valuedi.domain.connection.entity.SyncLog;
import org.umc.valuedi.domain.connection.enums.SyncStatus;
import org.umc.valuedi.domain.connection.enums.SyncType;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.SyncLogRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class SyncLogCommandService {

    private final MemberRepository memberRepository;
    private final SyncLogRepository syncLogRepository;

    /**
     * 동기화 로그 초기 생성 (작업 시작 시 호출)
     */
    public Long startSyncLog(Long memberId, SyncType syncType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        SyncLog syncLog = SyncLogConverter.toEntity(member, syncType);

        return syncLogRepository.save(syncLog).getId();
    }

    /**
     * 동가화 성공 업데이트
     */
    public void updateToSuccess(Long syncLogId) {
        SyncLog syncLog = findSyncLog(syncLogId);
        syncLog.markAsSuccess();

        // 성공 시 유저의 마지막 동기화 시간 업데이트
        syncLog.getMember().updateLastSyncedAt();
    }

    /**
     * 동기화 실패 업데이트
     */
    public void updateToFailed(Long logId, String errorMessage) {
        SyncLog syncLog = findSyncLog(logId);
        syncLog.markAsFailed(errorMessage);
    }

    private SyncLog findSyncLog(Long logId) {
        return syncLogRepository.findById(logId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.SYNC_LOG_NOT_FOUND));
    }
}
