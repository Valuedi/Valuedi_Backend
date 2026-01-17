package org.umc.valuedi.domain.savings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "savings_option")
public class SavingsOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저축 금리 유형
    @Column(name = "intr_rate_type", nullable = false, length = 10)
    private String intrRateType;

    // 저축 금리 유형명
    @Column(name = "intr_rate_type_nm", length = 30)
    private String intrRateTypeNm;

    // 적립 유형
    @Column(name = "rsrv_type", nullable = false, length = 10)
    private String rsrvType;

    // 적립 유형명
    @Column(name = "rsrv_type_nm", length = 30)
    private String rsrvTypeNm;

    // 저축 기간 (단위 : 개월)
    @Column(name = "save_trm", nullable = false)
    private Integer saveTrm;

    // 저축 금리
    @Column(name = "intr_rate")
    private Double intrRate;

    // 최고 우대금리
    @Column(name = "intr_rate2")
    private Double intrRate2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fin_prdt_cd", referencedColumnName = "fin_prdt_cd", nullable = false)
    private Savings savings;

    void setSavings(Savings savings) {
        this.savings = savings;
    }
}
