package org.umc.valuedi.domain.mbti.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_mbti_response")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberMbtiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // test_id FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private MemberMbtiTest test;

    // question_id FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private MbtiQuestion question;

    /**
     * 1: 매우 그렇다, 2: 그렇다, 3: 보통이다, 4: 아니다, 5: 매우 아니다
     */
    @Column(name = "choice_value", nullable = false)
    private Integer choiceValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void validate() {
        if (choiceValue == null || choiceValue < 1 || choiceValue > 5) {
            throw new IllegalArgumentException("choice_value must be between 1 and 5");
        }
    }
}
