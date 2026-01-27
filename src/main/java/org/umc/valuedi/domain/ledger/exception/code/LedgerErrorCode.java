package org.umc.valuedi.domain.ledger.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum LedgerErrorCode implements BaseErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "LEDGER404_1", "카테고리가 존재하지 않습니다."),




    INVALID_SYNC_REQUEST(HttpStatus.BAD_REQUEST, "LEDGER400_1","동기화 요청 파라미터가 잘못되었습니다. (yearMonth 또는 fromDate/toDate 필수");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
