package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AssetErrorCode implements BaseErrorCode {

    // ASSET
    ASSET_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_1", "자산 동기화 중 오류가 발생했습니다."),
    ASSET_BANK_ACCOUNT_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_2", "은행 계좌 목록 동기화에 실패했습니다."),
    ASSET_BANK_TRANSACTION_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_3", "은행 거래 내역 동기화에 실패했습니다."),
    ASSET_CARD_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_4", "카드 목록 동기화에 실패했습니다."),
    ASSET_CARD_APPROVAL_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_5", "카드 승인 내역 동기화에 실패했습니다."),

    ASSET_JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_6", "자산 데이터 파싱 중 오류가 발생했습니다."),
    ASSET_MATCHING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ASSET500_7", "자산과 거래 내역 매칭 중 오류가 발생했습니다."),

    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSET404_1", "존재하지 않는 자산입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
