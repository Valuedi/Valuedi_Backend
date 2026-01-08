package org.umc.valuedi.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.umc.valuedi.domain.member.enums.Gender;
import org.umc.valuedi.domain.member.enums.SignupType;
import org.umc.valuedi.domain.member.enums.Status;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "real_name", length = 50, nullable = false)
    private String realName;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "signup_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SignupType signupType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
