package org.umc.valuedi.domain.mbti.entity;

import jakarta.persistence.*;
import lombok.*;
import org.umc.valuedi.domain.mbti.enums.MbtiType;

@Entity
@Table(name = "mbti_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MbtiTypeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_type")
    private Long id;

    // 영문 MBTI
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private MbtiType type;

    // 별명
    @Column(name = "title", nullable = false, length = 40)
    private String title;

    @Column(name = "tagline", nullable = false, length = 100)
    private String tagline;

    @Lob
    @Column(name = "detail", nullable = false)
    private String detail;

    @Column(name = "warning", nullable = false, length = 500)
    private String warning;

    @Column(name = "recommend", nullable = false, length = 500)
    private String recommend;
}
