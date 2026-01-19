package org.umc.valuedi.domain.asset.bank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.asset.bank.enums.TransactionDirection;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.umc.valuedi.domain.asset.bank.enums.TransactionDirection.IN;
import static org.umc.valuedi.domain.asset.bank.enums.TransactionDirection.OUT;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "bank_transaction")
public class BankTransaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tr_date", nullable = false)
    private LocalDate trDate;

    @Column(name = "tr_time", nullable = false)
    private LocalTime trTime;

    @Column(name = "tr_datetime", nullable = false)
    private LocalDateTime trDatetime;

    @Column(name = "out_amount", nullable = false)
    @Builder.Default
    private Long outAmount = 0L;

    @Column(name = "in_amount", nullable = false)
    @Builder.Default
    private Long inAmount = 0L;

    @Column(name = "after_balance")
    private Long afterBalance;

    // 보낸분/받는분
    @Column(name = "desc1", length = 200)
    private String desc1;

    // 거래구분/메모: 오픈뱅킹, 펌뱅킹, 체크우리, 모바일 등
    @Column(name = "desc2", length = 200)
    private String desc2;

    // 적요(거래내용): 네이버페이 결제, 토스 OOO 등
    @Column(name = "desc3", length = 200)
    private String desc3;

    // 거래점: 하나은행, OO지점 등
    @Column(name = "desc4", length = 200)
    private String desc4;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private TransactionDirection direction;

    @Column(name = "order_seq")
    private Integer orderSeq;

    @Column(name = "raw_json", columnDefinition = "JSON")
    private String rawJson;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @PrePersist
    @PreUpdate
    private void calculateDirection() {
        if (this.direction == null) {
            this.direction = this.inAmount > 0 ? IN : OUT;
        }
    }
}
