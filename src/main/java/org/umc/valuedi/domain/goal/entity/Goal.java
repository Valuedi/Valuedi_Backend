package org.umc.valuedi.domain.goal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE goal SET deleted_at = NOW() WHERE id = ?")
public class Goal extends BaseEntity {

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
    private Long targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "color", nullable = true, length = 20)
    private String color;

    @Column(name = "icon", nullable = true)
    private Integer icon;

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

    public void changeTargetAmount(Long targetAmount) {
        this.targetAmount = targetAmount;
    }

    public void Activate() {
        this.status = GoalStatus.ACTIVE;
        this.completedAt = null;
    }

    // 실패 종료
    public void Complete() {
        this.status = GoalStatus.COMPLETE;
        this.completedAt = LocalDateTime.now();
    }

    // 취소 종료
    public void Fail() {
        this.status = GoalStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

}
