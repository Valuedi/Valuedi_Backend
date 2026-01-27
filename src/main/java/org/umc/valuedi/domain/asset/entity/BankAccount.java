package org.umc.valuedi.domain.asset.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.domain.asset.enums.AccountGroup;
import org.umc.valuedi.global.entity.BaseEntity;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "bank_account")
@SQLDelete(sql = "UPDATE bank_account SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_active = true")
public class BankAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계좌군
    @Column(name = "account_group", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountGroup accountGroup = AccountGroup.DEPOSIT_TRUST;

    // 계좌번호 (표시용)
    @Column(name = "account_display", nullable = false, length = 50)
    private String accountDisplay;

    // 계좌번호 원문 (양방향 암호화)
    @Column(name = "account_no_enc", columnDefinition = "BLOB")
    private byte[] accountNoEnc;

    // 계좌번호 원문 (단방향 해시)
    @Column(name = "account_no_hash", nullable = false, length = 64)
    private String accountNoHash;

    // 계좌명
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    // 통화코드
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "KRW";

    // 현재잔액
    @Column(name = "balance_amount")
    private Long balanceAmount;

    // 예금구분 코드
    @Column(name = "account_deposit_code", nullable = false, length = 10)
    private String accountDepositCode;

    // 최종 거래일
    @Column(name = "last_tran_date")
    private LocalDate lastTranDate;

    // 마이너스 통장 여부
    @Column(name = "is_overdraft", nullable = false)
    @Builder.Default
    private Boolean isOverdraft = false;

    // 마지막 동기화 시각
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    // 활성 여부
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 원본 JSON
    @Column(name = "raw_json", columnDefinition = "JSON")
    private String rawJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codef_connection_id", nullable = false)
    private CodefConnection codefConnection;

    public void assignConnection(CodefConnection connection) {
        this.codefConnection = connection;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateLastSyncedAt(LocalDateTime time) {
        this.lastSyncedAt = time;
    }
}
