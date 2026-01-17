package org.umc.valuedi.domain.mbti.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.umc.valuedi.domain.mbti.enums.MbtiQuestionCategory;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "mbti_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MbtiQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 100)
    private MbtiQuestionCategory category;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        if (isActive == null) isActive = true;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateCategory(MbtiQuestionCategory category) {
        this.category = category;
    }
}
