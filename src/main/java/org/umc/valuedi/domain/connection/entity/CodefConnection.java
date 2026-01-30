package org.umc.valuedi.domain.connection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;
import org.umc.valuedi.domain.connection.enums.ConnectionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.umc.valuedi.domain.connection.enums.ConnectionStatus.ACTIVE;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "codef_connection")
@SQLDelete(sql = "UPDATE codef_connection SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("status <> 'DELETED'")
public class CodefConnection extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization", nullable = false, length = 10)
    private String organization;

    @Column(name = "connected_id", nullable = false, length = 64)
    private String connectedId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConnectionStatus status = ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type",nullable = false, length = 10)
    private BusinessType businessType;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "last_error_code", length = 20)
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 300)
    private String lastErrorMessage;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "codefConnection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccount> bankAccountList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "codefConnection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cardList = new ArrayList<>();

    public void assignMember(Member member) {
        this.member = member;
    }

    public void addBankAccount(BankAccount account) {
        this.bankAccountList.add(account);
        account.assignConnection(this);
    }

    public void addCard(Card card) {
        this.cardList.add(card);
        card.assignConnection(this);
    }

    public void updateLastSyncedAt(LocalDateTime time) {
        this.lastSyncedAt = time;
    }
}