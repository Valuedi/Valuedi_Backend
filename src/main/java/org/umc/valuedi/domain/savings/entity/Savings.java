package org.umc.valuedi.domain.savings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "savings")
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 금융회사 명
    @Column(name = "kor_co_nm", nullable = false, length = 100)
    private String korCoNm;

    // 금융상품 코드
    @Column(name = "fin_prdt_cd", nullable = false, length = 50, unique = true)
    private String finPrdtCd;

    // 금융 상품명
    @Column(name = "fin_prdt_nm", nullable = false, length = 150)
    private String finPrdtNm;

    // 가입 방법
    @Column(name = "join_way", nullable = false, length = 200)
    private String joinWay;

    // 만기 후 이자율
    @Column(name = "mtrt_int", columnDefinition = "TEXT")
    private String mtrtInt;

    // 우대 조건
    @Column(name = "spcl_cnd", columnDefinition = "TEXT")
    private String spclCnd;

    // 가입 제한
    @Column(name = "join_deny", length = 10)
    private String joinDeny;

    // 가입 대상
    @Column(name = "join_member", length = 200)
    private String joinMember;

    // 기타 유의사항
    @Column(name = "etc_note", columnDefinition = "TEXT")
    private String etcNote;

    // 최고한도
    @Column(name = "max_limit")
    private Integer maxLimit;

    // 적립 유형
    @Column(name = "rsrv_type", length = 10)
    private String rsrvType;

    // 적립 유형명
    @Column(name = "rsrv_type_nm", length = 30)
    private String rsrvTypeNm;

    // 적재 시각
    @Column(name = "loaded_at", nullable = false)
    private LocalDateTime loadedAt;

    @OneToMany(mappedBy = "savings", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SavingsOption> savingsOptionList = new ArrayList<>();

    public void replaceOptions(List<SavingsOption> newOptions) {
        this.savingsOptionList.clear();
        if (newOptions == null) return;
        newOptions.forEach(this::addOption);
    }

    public void addOption(SavingsOption savingsOption) {
        savingsOption.setSavings(this);
        this.savingsOptionList.add(savingsOption);
    }

    public void updateBasicInfo(
            String korCoNm,
            String finPrdtCd,
            String finPrdtNm,
            String joinWay,
            String mtrtInt,
            String spclCnd,
            String joinDeny,
            String joinMember,
            String etcNote,
            Integer maxLimit,
            String rsrvType,
            String rsrvTypeNm,
            LocalDateTime loadedAt
    ) {
        this.korCoNm = korCoNm;
        this.finPrdtCd = finPrdtCd;
        this.finPrdtNm = finPrdtNm;
        this.joinWay = joinWay;
        this.mtrtInt = mtrtInt;
        this.spclCnd = spclCnd;
        this.joinDeny = joinDeny;
        this.joinMember = joinMember;
        this.etcNote = etcNote;
        this.maxLimit = maxLimit;
        this.rsrvType = rsrvType;
        this.rsrvTypeNm = rsrvTypeNm;
        this.loadedAt = loadedAt;
    }
}
