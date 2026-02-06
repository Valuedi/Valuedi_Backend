package org.umc.valuedi.domain.asset.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseSuccessCode;


@Getter
@AllArgsConstructor
public enum AssetSuccessCode implements BaseSuccessCode {

    SYNC_REQUEST_SUCCESS(HttpStatus.OK, "ASSET200_1", "자산 동기화 요청이 성공적으로 접수되었습니다. 잠시 후 데이터를 확인해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
