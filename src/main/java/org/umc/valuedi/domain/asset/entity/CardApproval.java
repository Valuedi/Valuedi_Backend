package org.umc.valuedi.domain.asset.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.asset.enums.HomeForeignType;
import org.umc.valuedi.domain.asset.enums.PaymentType;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "card_approval",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_card_approval_identity",
                        columnNames = {"card_id", "approval_no"}
                )
        }
)
public class CardApproval extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "used_date", nullable = false)
    private LocalDate usedDate;

    @Column(name = "used_time", nullable = false)
    private LocalTime usedTime;

    @Column(name = "used_datetime", nullable = false)
    private LocalDateTime usedDatetime;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    // 결제방법
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20)
    private PaymentType paymentType;

    // 할부개월
    @Column(name = "installment_month")
    private Integer installmentMonth;

    @Column(name = "approval_no", length = 50)
    private String approvalNo;

    // 국내/해외
    @Enumerated(EnumType.STRING)
    @Column(name = "home_foreign_type", length = 20)
    @Builder.Default
    private HomeForeignType homeForeignType = HomeForeignType.DOMESTIC;

    @Column(name = "currency", length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_yn", nullable = false, length = 20)
    private CancelStatus cancelYn;

    @Column(name = "cancel_amount")
    private Long cancelAmount;

    @Column(name = "merchant_corp_no", length = 20)
    private String merchantCorpNo;

    @Column(name = "merchant_name", length = 200)
    private String merchantName;

    @Column(name = "merchant_type", nullable = false, length = 50)
    private String merchantType;

    @Column(name = "merchant_no", length = 100)
    private String merchantNo;

    @Column(name = "comm_start_date")
    private LocalDateTime commStartDate;

    @Column(name = "comm_end_date")
    private LocalDateTime commEndDate;

    @Column(name = "raw_json", columnDefinition = "JSON")
    private String rawJson;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
}
