package org.umc.valuedi.domain.connection.converter;

import org.umc.valuedi.domain.connection.dto.res.SyncLogResDTO;
import org.umc.valuedi.domain.connection.entity.SyncLog;
import org.umc.valuedi.domain.connection.enums.SyncStatus;
import org.umc.valuedi.domain.connection.enums.SyncType;
import org.umc.valuedi.domain.member.entity.Member;

public class SyncLogConverter {

    public static SyncLogResDTO.SyncLogResponseDTO toSyncLogResponseDTO(SyncLog syncLog) {
        return SyncLogResDTO.SyncLogResponseDTO.builder()
                .syncLogId(syncLog.getId())
                .syncStatus(syncLog.getSyncStatus().name())
                .syncType(syncLog.getSyncType().name())
                .errorMessage(syncLog.getErrorMessage())
                .updatedAt(syncLog.getUpdatedAt())
                .build();
    }

    // Member와 Type을 받아 초기 IN_PROGRESS 상태의 엔티티로 변환
    public static SyncLog toEntity(Member member, SyncType syncType) {
        return SyncLog.builder()
                .member(member)
                .syncStatus(SyncStatus.IN_PROGRESS)
                .syncType(syncType)
                .build();
    }
}
