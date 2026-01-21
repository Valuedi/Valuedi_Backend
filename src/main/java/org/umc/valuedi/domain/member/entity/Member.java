package org.umc.valuedi.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.domain.member.enums.*;
import org.umc.valuedi.domain.terms.entity.MemberTerms;
import org.umc.valuedi.global.entity.BaseEntity;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET deleted_at = CURRENT_TIMESTAMP, status = 'DELETED' WHERE id = ?")
@SQLRestriction("status <> 'DELETED'")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "real_name", length = 50, nullable = false)
    private String realName;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "signup_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SignupType signupType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "withdrawal_reason")
    @Enumerated(EnumType.STRING)
    private WithdrawalReason withdrawalReason;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTerms> memberTermsList = new ArrayList<>();
  
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodefConnection> codefConnectionList = new ArrayList<>();

    public void addCodefConnection(CodefConnection connection) {
        this.codefConnectionList.add(connection);
        connection.assignMember(this);
    }
}
