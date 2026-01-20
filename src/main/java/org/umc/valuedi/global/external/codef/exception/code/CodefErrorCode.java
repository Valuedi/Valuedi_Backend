package org.umc.valuedi.global.external.codef.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CodefErrorCode implements BaseErrorCode {

    CODEF_ENCRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_1", "CODEF 연동을 위한 비밀번호 암호화 중 오류가 발생했습니다."),
    CODEF_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_2","CODEF Access Token 발급 중 오류가 발생했습니다."),
    CODEF_API_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_3", "CODEF 서버 내부에서 오류가 발생했습니다."),

    CODEF_API_CREATE_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_1", "금융 계정 최초 등록에 실패했습니다."),
    CODEF_API_ADD_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_2", "금융 계정 추가 등록에 실패했습니다."),
    CODEF_API_DELETE_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_3", "금융 계정 연동 해제에 실패했습니다."),

    CODEF_DUPLICATE_ORGANIZATION(HttpStatus.CONFLICT, "CODEF409_1", "이미 연동된 금융사입니다. 기존 연동을 먼저 해제해주세요."),
    CODEF_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "CODEF400_4", "금융사 로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요."),

    CODEF_ORGANIZATION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "CODEF503_1", "금융사 시스템 점검 중입니다. 잠시 후 다시 시도해주세요.")

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
