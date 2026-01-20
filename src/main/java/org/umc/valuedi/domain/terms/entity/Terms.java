package org.umc.valuedi.domain.terms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.global.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "terms")
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "terms")
    @Builder.Default
    private List<MemberTerms> memberTermsList = new ArrayList<>();
}
