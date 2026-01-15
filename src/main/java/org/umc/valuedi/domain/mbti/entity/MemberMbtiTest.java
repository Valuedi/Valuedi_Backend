package org.umc.valuedi.domain.mbti.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member_mbti_test")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberMbtiTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 40)
    private MbtiType resultType;

    @Column(name = "anxiety_score", nullable = false)
    private Integer anxietyScore;

    @Column(name = "stability_score", nullable = false)
    private Integer stabilityScore;

    @Column(name = "impulse_score", nullable = false)
    private Integer impulseScore;

    @Column(name = "planning_score", nullable = false)
    private Integer planningScore;

    @Column(name = "aggressive_score", nullable = false)
    private Integer aggressiveScore;

    @Column(name = "conservative_score", nullable = false)
    private Integer conservativeScore;

    @Column(name = "avoidance_score", nullable = false)
    private Integer avoidanceScore;

    @Column(name = "rational_score", nullable = false)
    private Integer rationalScore;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberMbtiResponse> responses = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (isActive == null) isActive = true;
        if (anxietyScore == null) anxietyScore = 0;
        if (stabilityScore == null) stabilityScore = 0;
        if (impulseScore == null) impulseScore = 0;
        if (planningScore == null) planningScore = 0;
        if (aggressiveScore == null) aggressiveScore = 0;
        if (conservativeScore == null) conservativeScore = 0;
        if (avoidanceScore == null) avoidanceScore = 0;
        if (rationalScore == null) rationalScore = 0;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public void setAsRepresentative() {
        this.isActive = true;
    }

    public void unsetRepresentative() {
        this.isActive = false;
    }

    public void addResponse(MemberMbtiResponse response) {
        this.responses.add(response);
        response.setTest(this);
    }

    public void removeResponse(MemberMbtiResponse response) {
        this.responses.remove(response);
    }
}
