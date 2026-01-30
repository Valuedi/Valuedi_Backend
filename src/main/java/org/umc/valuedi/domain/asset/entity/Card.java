package org.umc.valuedi.domain.asset.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.global.entity.BaseEntity;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(
        name = "card",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_card_identity",
                        columnNames = {"codef_connection_id", "card_no_masked"}
                )
        }
)
@SQLDelete(sql = "UPDATE card SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_active = true")
public class Card extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_no_masked", length = 30)
    private String cardNoMasked;

    @Column(name = "card_name", length = 150)
    private String cardName;

    @Column(name = "card_type", length = 50)
    private String cardType;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "raw_json", columnDefinition = "JSON")
    private String rawJson;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

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
