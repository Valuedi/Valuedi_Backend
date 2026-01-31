package org.umc.valuedi.domain.savings.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.savings.enums.ReasonCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommendation_reason")
public class RecommendationReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 30)
    private ReasonCode reasonCode;

    @Column(name = "reason_text", nullable = false, length = 200)
    private String reasonText;

    @Column(name = "delta", precision = 6, scale = 3)
    private BigDecimal delta;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }
}
