package org.umc.valuedi.global.external.fss.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum FssErrorCode implements BaseErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "FSS400_1", "금감원 API 요청 파라미터가 올바르지 않습니다."),  // 100/101
    INVALID_AUTH(HttpStatus.UNAUTHORIZED, "FSS401_1", "금감원 인증키가 유효하지 않습니다."),  // 010/011/012/013
    NOT_ALLOWED_IP(HttpStatus.FORBIDDEN, "FSS403_1", "허용되지 않은 IP에서 요청했습니다."),  // 021
    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "FSS429_1", "금감원 API 일일 호출 한도를 초과했습니다."),  // 020
    FSS_INTERNAL_ERROR(HttpStatus.BAD_GATEWAY, "FSS502_1", "금감원 API 내부 오류입니다."),  // 900
    EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "FSS502_2", "금감원 API 응답이 비어 있습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    public static FssErrorCode fromErrCd(String errCd) {
        if (errCd == null) return FSS_INTERNAL_ERROR;
        return switch (errCd) {
            case "010", "011", "012", "013" -> INVALID_AUTH;
            case "020" -> DAILY_LIMIT_EXCEEDED;
            case "021" -> NOT_ALLOWED_IP;
            case "100", "101" -> INVALID_PARAMETER;
            case "900" -> FSS_INTERNAL_ERROR;
            default -> FSS_INTERNAL_ERROR;
        };
    }
}
