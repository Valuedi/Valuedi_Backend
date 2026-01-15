package org.umc.valuedi.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.auth.exception.code.AuthSuccessCode;
import org.umc.valuedi.domain.auth.service.command.AuthCommandService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.util.CookieUtil;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthCommandService authCommandService;
    private final KakaoProperties kakaoProperties;
    private final CookieUtil cookieUtil;

    @GetMapping("/oauth/kakao/login")
    public ApiResponse<String> kakaoLogin(HttpServletResponse response) {
        String state = UUID.randomUUID().toString();
        cookieUtil.addCookie(response, "oauth_state", state, 600);
        String loginUrl = kakaoProperties.getKakaoAuthUrl(state);

        return ApiResponse.onSuccess(AuthSuccessCode.KAKAO_AUTH_URL_SUCCESS, loginUrl);
    }

    @GetMapping("/oauth/kakao/callback")
    public ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @CookieValue(name = "oauth_state", required = false) String oauthState,
            HttpServletResponse response
    ) {
        cookieUtil.deleteCookie(response, "oauth_state");

        if (oauthState == null || !oauthState.equals(state)) {
            throw new AuthException(AuthErrorCode.INVALID_STATE);
        }

        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_OK, authCommandService.loginKakao(code));
    }
}
