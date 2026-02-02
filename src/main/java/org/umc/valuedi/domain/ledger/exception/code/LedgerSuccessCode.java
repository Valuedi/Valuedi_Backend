package org.umc.valuedi.domain.ledger.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;

@Getter
@AllArgsConstructor
public enum LedgerSuccessCode implements BaseSuccessCode {

    // 조회 성공
    LEDGER_LIST_FETCHED(HttpStatus.OK, "LEDGER200_1", "거래 내역 조회에 성공했습니다."),
    LEDGER_SUMMARY_FETCHED(HttpStatus.OK, "LEDGER200_2", "월 소비 내역 요약 조회에 성공했습니다."),
    CATEGORY_STATS_FETCHED(HttpStatus.OK, "LEDGER200_3", "카테고리별 소비 집계 조회에 성공했습니다."),
    DAILY_STATS_FETCHED(HttpStatus.OK, "LEDGER200_4", "일별 수입/지출 합계 조회에 성공했습니다."),
    TREND_FETCHED(HttpStatus.OK, "LEDGER200_5", "월별 지출 추이 조회에 성공했습니다."),
    TOP_CATEGORY_FETCHED(HttpStatus.OK, "LEDGER200_6", "최다 소비 항목 조회에 성공했습니다."),
    PEER_COMPARE_FETCHED(HttpStatus.OK, "LEDGER200_7", "또래 비교 조회에 성공했습니다."),

    // 동기화 성공
    LEDGER_SYNC_SUCCESS(HttpStatus.OK, "LEDGER200_8", "거래내역 동기화가 완료되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
