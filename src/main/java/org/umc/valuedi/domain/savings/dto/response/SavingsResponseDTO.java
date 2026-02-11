package org.umc.valuedi.domain.savings.dto.response;

import lombok.Builder;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;

import java.math.BigDecimal;
import java.util.List;

public class SavingsResponseDTO {

    @Builder
    public record SavingsListResponse(
            int totalCount,  // 총 상품건수
            int nowPageNo,  // 현재 페이지
            boolean hasNext,  // 다음 페이지 존재 여부
            List<RecommendedSavingProduct> products // 상품 목록
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

    @Builder
    public record SavingsDetailResponse(
            String korCoNm,
            String finPrdtCd,
            String finPrdtNm,
            String joinWay,
            String mtrtInt,
            String spclCnd,
            String joinDeny,
            String joinMember,
            String etcNote,
            String maxLimit,
            List<Option> options
    ) {
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

    @Builder
    public record RecommendResponse(
            List<RecommendedProduct> products,
            String rationale
    ) {}

    @Builder
    public record RecommendedProduct(
            String korCoNm,
            String finPrdtCd,
            String finPrdtNm,
            String rsrvType,
            String rsrvTypeNm,
            BigDecimal score
    ) {}
}
