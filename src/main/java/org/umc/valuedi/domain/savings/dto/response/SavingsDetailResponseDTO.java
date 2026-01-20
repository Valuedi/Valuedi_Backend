package org.umc.valuedi.domain.savings.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SavingsDetailResponseDTO(
        SavingProductDetail product  // 상품
) {
    // 적금 상품 상세 조회
    public record SavingProductDetail (
            String korCoNm,  // 금융회사 명
            String finPrdtCd,  // 금융상품 코드
            String finPrdtNm,  // 금융 상품명
            String joinWay,  // 가입 방법
            String mtrtInt,  // 만기 후 이자율
            String spclCnd,  // 우대조건
            String joinDeny,  // 가입 제한
            String joinMember,  // 가입대상
            String etcNote,  // 기타 유의사항
            String maxLimit,  // 최고한도
            List<Option> options
    ) {}

    public record Option (
            String intrRateType,  // 저축 금리 유형
            String intrRateTypeNm,  // 저축 금리 유형명
            String rsrvType,  // 적립 유형
            String rsrvTypeNm,  // 적립 유형명
            String saveTrm,  // 저축 기간 (단위 : 개월)
            Double intrRate,  // 저축 금리
            Double intrRate2  // 최고 우대금리
    ) {}
}
