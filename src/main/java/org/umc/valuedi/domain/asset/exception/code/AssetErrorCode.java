package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AssetErrorCode implements BaseErrorCode {

    SYNC_COOL_DOWN(HttpStatus.TOO_MANY_REQUESTS, "ASSET429_1", "전체 동기화는 10분에 한 번만 요청할 수 있습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSET404_1", "존재하지 않거나 접근 권한이 없는 계좌입니다."),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSET404_2", "존재하지 않거나 접근 권한이 없는 카드입니다."),
    ASSET_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSET404_3", "시스템 필수 카테고리가 DB에 존재하지 않습니다. 관리자에게 문의하세요"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
