package org.umc.valuedi.domain.connection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.connection.entity.SyncLog;
import java.util.Optional;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
    // 가장 최근 동기화 로그 1건 조회
    Optional<SyncLog> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);
}
