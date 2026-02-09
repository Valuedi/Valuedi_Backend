package org.umc.valuedi.domain.savings.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;
import org.umc.valuedi.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "recommendation_batch")
public class RecommendationBatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_mbti_test_id", nullable = false)
    private Long memberMbtiTestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_status", nullable = false, length = 20)
    private RecommendationStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public static RecommendationBatch pending(Long memberId, Long memberMbtiTestId) {
        RecommendationBatch b = new RecommendationBatch();
        b.memberId = memberId;
        b.memberMbtiTestId = memberMbtiTestId;
        b.status = RecommendationStatus.PENDING;
        return b;
    }

    public boolean isPendingOrProcessing() {
        return status == RecommendationStatus.PENDING || status == RecommendationStatus.PROCESSING;
    }

    public void markProcessing() {
        this.status = RecommendationStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void markSuccess() {
        this.status = RecommendationStatus.SUCCESS;
        this.errorMessage = null;
    }

    public void markFailed(String msg) {
        this.status = RecommendationStatus.FAILED;
        this.errorMessage = msg;
    }
}
