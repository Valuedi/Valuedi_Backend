package org.umc.valuedi.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.BaseErrorCode;

import java.io.IOException;

@Component
public class SecurityExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendErrorResponse(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        ApiResponse<Void> errorResponse = ApiResponse.onFailure(errorCode, null);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
