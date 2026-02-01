package org.umc.valuedi.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400_1", "잘못된 요청입니다."),
    INVALID_DATA_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400_2", "데이터 제약 조건을 위반했습니다. 입력값을 다시 확인해주세요."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404_1", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405_1", "허용되지 않은 요청 방식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500_1", "예기치 않은 서버 에러가 발생했습니다."),
    DB_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500_2", "DB 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401_1", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH403_1", "요청이 거부되었습니다."),

    // Validation
    VALID_FAIL(HttpStatus.BAD_REQUEST, "VALID400_1", "검증에 실패했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}