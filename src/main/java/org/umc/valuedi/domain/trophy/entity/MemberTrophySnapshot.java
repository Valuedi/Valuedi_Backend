package org.umc.valuedi.domain.trophy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.trophy.enums.PeriodType;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_trophy_snapshot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "trophy_id", "period_type", "period_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTrophySnapshot { // BaseEntity 상속 여부는 정책에 따라 결정 (불변 성격이 강함)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trophy_id", nullable = false)
    private Trophy trophy;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "period_key", nullable = false)
    private String periodKey;

    @Column(name = "achieved_count")
    private int achievedCount;

    @Column(name = "metric_value")
    private String metricValue;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public MemberTrophySnapshot(Member memberId, Trophy trophy, PeriodType periodType, String periodKey, int achievedCount, String metricValue) {
        this.memberId = memberId;
        this.trophy = trophy;
        this.periodType = periodType;
        this.periodKey = periodKey;
        this.achievedCount = achievedCount;
        this.metricValue = metricValue;
    }

    public void updateSnapshot(int achievedCount, String metricValue) {
        this.achievedCount = achievedCount;
        this.metricValue = metricValue;
    }
}
