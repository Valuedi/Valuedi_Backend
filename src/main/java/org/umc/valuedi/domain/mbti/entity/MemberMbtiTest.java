package org.umc.valuedi.domain.mbti.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.umc.valuedi.domain.mbti.enums.MbtiType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member_mbti_test")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MemberMbtiTest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MemberMbtiResponse> responses = new ArrayList<>();


    public void addResponse(MemberMbtiResponse response) {
        this.responses.add(response);
        response.setTest(this);
    }

}
