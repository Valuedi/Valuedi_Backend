package org.umc.valuedi.domain.savings.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SavingsListResponse(
        int totalCount,  // 총 상품건수
        int maxPageNo,  // 총 페이지 건수
        int nowPageNo,  // 현재 조회 페이지 번호
        List<RecommendedSavingProduct> products  // 상품 목록
) {
    // 상품 목록 조회
    public record RecommendedSavingProduct(
            String korCoNm,  // 금융회사 명
            String finPrdtCd,  // 금융상품 코드
            String finPrdtNm,  // 금융 상품명
            String rsrvType,  // 적립 유형
            String rsrvTypeNm  // 적립 유형명
    ) {}
}
