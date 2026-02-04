package org.umc.valuedi.domain.connection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.connection.enums.SyncStatus;
import org.umc.valuedi.domain.connection.enums.SyncType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "sync_log")
public class SyncLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, length = 20)
    private SyncType syncType;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 상태 업데이트 메서드들
    public void markAsSuccess() {
        this.syncStatus = SyncStatus.SUCCESS;
        this.errorMessage = null; // 성공 시 에러 메시지 비움
    }

    public void markAsFailed(String errorDetail) {
        this.syncStatus = SyncStatus.FAILED;
        this.errorMessage = errorDetail;
    }
}
