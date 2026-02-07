package org.umc.valuedi.domain.savings.dto.response;

import lombok.Builder;
import org.umc.valuedi.domain.savings.enums.RecommendationStatus;

import java.math.BigDecimal;
import java.util.List;

public class SavingsResponseDTO {

    @Builder
    public record SavingsListResponse(
            int totalCount,  // 총 상품건수
            int maxPageNo,  // 총 페이지 건수
            int nowPageNo,  // 현재 조회 페이지 번호
            List<RecommendedSavingProduct> products,  // 상품 목록
            RecommendationStatus status,  // 상품 추천 상태 (PENDING | SUCCESS | FAILED)
            String message  // 상품 추천 상태 메시지
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

    @Builder
    public record TriggerResponse(
            Long batchId,
            RecommendationStatus status,  // PENDING | SUCCESS | FAILED
            String message
    ) {}

    @Builder
    public record TriggerDecision(
            Long batchId,
            RecommendationStatus status,
            String message,
            boolean shouldStartAsync
    ) {}
}
