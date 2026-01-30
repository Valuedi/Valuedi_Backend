package org.umc.valuedi.global.external.codef.util;

import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;

import java.util.function.Supplier;

@Slf4j
@Component
public class CodefApiExecutor {

    public <T> CodefApiResponse<T> execute(Supplier<CodefApiResponse<T>> apiCall) {
        try {
            CodefApiResponse<T> response = apiCall.get();
            if (response == null) {
                log.warn("CODEF API 응답이 null입니다.");
                throw new CodefException(CodefErrorCode.CODEF_RESPONSE_EMPTY);
            }
            return response;
        } catch (RetryableException e) {
            log.error("CODEF API 호출 중 재시도 가능한 오류 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        } catch (FeignException e) {
            log.error("CODEF API 호출 실패 - Status: {}, Body: {}", e.status(), e.contentUTF8(), e);
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("CODEF API 호출 중 알 수 없는 오류 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_API_UNHANDLED_ERROR);
        }
    }
}
