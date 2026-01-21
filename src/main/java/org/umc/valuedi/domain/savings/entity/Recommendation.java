package org.umc.valuedi.domain.savings.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.member.entity.Member;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommendation")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score", precision = 6, scale = 3)
    private BigDecimal score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fin_prdt_cd", referencedColumnName = "fin_prdt_cd", nullable = false)
    private Savings savings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_option_id")
    private SavingsOption savingsOption;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecommendationReason> recommendationReasonList = new ArrayList<>();

    public void addReason(RecommendationReason reason) {
        reason.setRecommendation(this);
        this.recommendationReasonList.add(reason);
    }

    public void replaceReasons(List<RecommendationReason> newReasons) {
        this.recommendationReasonList.clear();
        if (newReasons == null) return;
        newReasons.forEach(this::addReason);
    }

    void setMember(Member member) {
        this.member = member;
    }

    void setSavings(Savings savings) {
        this.savings = savings;
    }

    void setSavingsOption(SavingsOption savingsOption) {
        this.savingsOption = savingsOption;
    }
}
