package org.umc.valuedi.domain.goal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "id2", nullable = true)
    private Long accountId;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "target_amount", nullable = false)
    private Integer targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "color", nullable = true, length = 20)
    private String color;

    @Column(name = "icon", nullable = true)
    private Integer icon;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void changeEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void changeTargetAmount(Integer targetAmount) {
        this.targetAmount = targetAmount;
    }

    public void changeStatus(GoalStatus status) {
        this.status = status;
        if (status == GoalStatus.COMPLETE) this.isCompleted = true;
        if (status == GoalStatus.ACTIVE || status == GoalStatus.CANCELED) this.isCompleted = false;
    }

}
