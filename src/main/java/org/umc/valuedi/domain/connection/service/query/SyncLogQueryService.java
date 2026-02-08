package org.umc.valuedi.domain.connection.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.converter.SyncLogConverter;
import org.umc.valuedi.domain.connection.dto.res.SyncLogResDTO;
import org.umc.valuedi.domain.connection.entity.SyncLog;
import org.umc.valuedi.domain.connection.enums.SyncStatus;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.SyncLogRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncLogQueryService {

    private final SyncLogRepository syncLogRepository;

    /**
     * 특정 회원의 현재 동기화 여부 확인
     * 가장 최근 로그가 존재하고, 그 상태가 IN_PROGRESS인 경우 true 반환
     */
    public boolean isMemberSyncing(Long memberId) {
        return syncLogRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId)
                .map(log -> log.getSyncStatus() == SyncStatus.IN_PROGRESS)
                .orElse(false);
    }

    /**
     * 상태 조회 API를 위한 상세 정보 조회 (상태 확인 폴링 API용)
     */
    public SyncLogResDTO.SyncLogResponseDTO getLatestSyncLog(Long memberId) {
        SyncLog syncLog = syncLogRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.SYNC_LOG_NOT_FOUND));

        return SyncLogConverter.toSyncLogResponseDTO(syncLog);
    }
}
