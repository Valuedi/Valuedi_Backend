package org.umc.valuedi.domain.trophy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.trophy.enums.TrophyType;
import org.umc.valuedi.global.entity.BaseEntity;

@Entity
@Table(name = "trophy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trophy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TrophyType type;

    private String description;

}
