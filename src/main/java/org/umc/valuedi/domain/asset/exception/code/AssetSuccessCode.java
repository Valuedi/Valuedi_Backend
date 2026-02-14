package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;


@Getter
@AllArgsConstructor
public enum AssetSuccessCode implements BaseSuccessCode {

    ACCOUNT_TRANSACTIONS_FETCHED(HttpStatus.OK, "ASSET200_1", "계좌 거래내역 조회에 성공했습니다."),
    CARD_TRANSACTIONS_FETCHED(HttpStatus.OK, "ASSET200_2", "카드 승인내역 조회에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
