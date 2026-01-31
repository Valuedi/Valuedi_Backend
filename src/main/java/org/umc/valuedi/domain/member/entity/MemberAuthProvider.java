package org.umc.valuedi.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.umc.valuedi.domain.member.enums.Provider;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "member_auth_provider")
@SQLDelete(sql = "UPDATE member_auth_provider SET unlinked_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("unlinked_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
public class MemberAuthProvider {

    private static final String ANONYMIZED_ID_PREFIX = "deleted_";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @CreatedDate
    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Column(name = "unlinked_at")
    private LocalDateTime unlinkedAt;

    public void anonymize() {
        this.providerUserId = ANONYMIZED_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8);
        this.providerEmail = null;
    }
}
