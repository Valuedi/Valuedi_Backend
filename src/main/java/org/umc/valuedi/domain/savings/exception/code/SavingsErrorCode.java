package org.umc.valuedi.domain.savings.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum SavingsErrorCode implements BaseErrorCode {

    SAVINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVINGS404_1", "해당 적금 상품을 찾을 수 없습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVINGS404_2", "아직 추천받은 내역이 없습니다. 먼저 상품 추천을 진행해 주세요."),
    FILTERED_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SAVINGS400_1", "해당 필터 조건에 맞는 추천 상품이 없습니다."),
    RECOMMENDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SAVINGS500_1", "AI 추천 생성에 실패했습니다. 다시 시도해 주세요.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
