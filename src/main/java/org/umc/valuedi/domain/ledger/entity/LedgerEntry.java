package org.umc.valuedi.domain.ledger.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.ledger.enums.TransactionType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "ledger_entry", indexes = {
        @Index(name = "idx_ledger_entry_member_transaction_at", columnList = "member_id, transaction_at DESC"),
        @Index(name = "idx_ledger_entry_member_category", columnList = "member_id, category_id"),
        @Index(name = "uq_ledger_entry_bank_transaction_id", columnList = "bank_transaction_id", unique = true),
        @Index(name = "uq_ledger_entry_card_approval_id", columnList = "card_approval_id", unique = true)
})
public class LedgerEntry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_transaction_id")
    private BankTransaction bankTransaction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_approval_id")
    private CardApproval cardApproval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 50)
    private String title;

    @Column(length = 200)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false) @Builder.Default
    private Boolean isUserModified = false;

    @Column(nullable = false)
    private LocalDateTime transactionAt;

    @Column(name = "canonical_key", length = 64, nullable = false)
    private String canonicalKey;

    @Column(name = "source_type", length = 10, nullable = false)
    @Builder.Default
    private String sourceType = "BANK"; //

}
