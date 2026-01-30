package org.umc.valuedi.global.external.codef.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CodefErrorCode implements BaseErrorCode {

    CODEF_ENCRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_1", "CODEF 연동을 위한 데이터 암호화 중 오류가 발생했습니다."),
    CODEF_DECRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_6", "CODEF 연동 데이터 복호화 중 오류가 발생했습니다."),
    CODEF_TOKEN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_2","CODEF Access Token 발급 중 오류가 발생했습니다."),
    CODEF_API_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_3", "CODEF 서버 내부에서 오류가 발생했습니다."),

    CODEF_API_CREATE_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_1", "금융 계정 최초 등록에 실패했습니다."),
    CODEF_API_ADD_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_2", "금융 계정 추가 등록에 실패했습니다."),
    CODEF_API_DELETE_FAILED(HttpStatus.BAD_REQUEST, "CODEF400_3", "금융 계정 연동 해제에 실패했습니다."),

    CODEF_DUPLICATE_ORGANIZATION(HttpStatus.CONFLICT, "CODEF409_1", "이미 연동된 금융사입니다. 기존 연동을 먼저 해제해주세요."),
    CODEF_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "CODEF400_4", "금융사 로그인 정보가 올바르지 않습니다. 아이디와 비밀번호를 확인해주세요."),

    CODEF_ORGANIZATION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "CODEF503_1", "금융사 시스템 점검 중입니다. 잠시 후 다시 시도해주세요."),

    CODEF_API_BANK_ACCOUNT_LIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_4", "보유 계좌 목록 조회에 실패했습니다."),
    CODEF_API_CARD_LIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_5", "보유 카드 목록 조회에 실패했습니다."),
    
    CODEF_JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_6", "CODEF 응답 데이터 파싱 중 오류가 발생했습니다."),
    CODEF_API_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_7", "CODEF API 서버와의 통신 중 오류가 발생했습니다."),
    CODEF_RESPONSE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_8", "CODEF API 응답이 비어있습니다."),
    CODEF_API_UNHANDLED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODEF500_9", "CODEF API 호출 중 알 수 없는 오류가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
