package org.umc.valuedi.global.external.codef.dto.res;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * CODEF API의 공통 응답 구조
 */
@Getter
@ToString(exclude = "data")
@NoArgsConstructor
public class CodefApiResponse<T> {

    private Result result;
    private T data;

    @Getter
    @ToString
    @NoArgsConstructor
    public static class Result {
        private String code;            // 응답 코드 (CF-00000)
        private String extraMessage;    // 추가 메시지
        private String message;         // 결과 메시지
        private String transactionId;   // CODEF 거래 고유 번호
    }

    /**
     * CODEF 응답이 성공(CF-00000)인지 확인
     */
    public boolean isSuccess() {
        return result != null && "CF-00000".equals(result.code);
    }
}
