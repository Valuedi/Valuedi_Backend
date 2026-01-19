package org.umc.valuedi.domain.trophy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_trophy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "trophy_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTrophy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trophy_id", nullable = false)
    private Trophy trophy;

    @Column(name = "trophy_count")
    private int trophyCount;

    @Column(name = "last_achieved_at")
    private LocalDateTime lastAchievedAt;

    public MemberTrophy(Long memberId, Trophy trophy) {
        this.memberId = memberId;
        this.trophy = trophy;
        this.trophyCount = 0;
    }

    public void accumulate(int count, LocalDateTime achievedAt) {
        this.trophyCount += count;
        this.lastAchievedAt = achievedAt;
    }
}
