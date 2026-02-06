package org.umc.valuedi.global.external.genai.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum GeminiErrorCode implements BaseErrorCode {

    EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "GEMINI502_1", "Gemini API 응답이 비어 있습니다."),
    GEMINI_CALL_FAILED(HttpStatus.BAD_GATEWAY, "GEMINI502_2", "Gemini API 호출에 실패했습니다."),
    GEMINI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "GEMINI504_1", "Gemini API 호출 시간이 초과되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
