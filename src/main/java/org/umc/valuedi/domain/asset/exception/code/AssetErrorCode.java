package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AssetErrorCode implements BaseErrorCode {

    SYNC_COOL_DOWN(HttpStatus.TOO_MANY_REQUESTS, "ASSET429_1", "전체 동기화는 10분에 한 번만 요청할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
