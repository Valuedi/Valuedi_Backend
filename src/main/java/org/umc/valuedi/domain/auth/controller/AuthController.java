package org.umc.valuedi.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.auth.exception.code.AuthSuccessCode;
import org.umc.valuedi.domain.auth.service.command.AuthCommandService;
import org.umc.valuedi.domain.auth.service.query.AuthQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.util.CookieUtil;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController implements AuthControllerDocs {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;
    private final KakaoProperties kakaoProperties;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    @Override
    @GetMapping("/oauth/kakao/login")
    public ApiResponse<String> kakaoLogin(HttpServletResponse response) {
        String state = UUID.randomUUID().toString();
        cookieUtil.addCookie(response, "oauth_state", state, 600, "/auth/oauth/kakao/callback");
        String loginUrl = kakaoProperties.getKakaoAuthUrl(state);

        return ApiResponse.onSuccess(AuthSuccessCode.KAKAO_AUTH_URL_SUCCESS, loginUrl);
    }

    @Override
    @GetMapping("/oauth/kakao/callback")
    public ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @CookieValue(name = "oauth_state", required = false) String oauthState,
            HttpServletResponse response
    ) {
        cookieUtil.deleteCookie(response, "oauth_state", "/auth/oauth/kakao/callback");

        if (oauthState == null || !oauthState.equals(state)) {
            throw new AuthException(AuthErrorCode.INVALID_STATE);
        }

        AuthResDTO.LoginResultDTO result = authCommandService.loginKakao(code);

        // 리프레시 토큰은 쿠키에 저장
        cookieUtil.addCookie(
                response,
                "refreshToken",
                result.refreshToken(),
                (int) jwtUtil.getRefreshTokenExpiration() / 1000,
                "/auth/token/refresh"
        );

        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_OK, result);
    }

    @Override
    @GetMapping("/check-username")
    public ApiResponse<Void> checkUsername(
            @RequestParam(name = "username")
            String username
    ) {
        authQueryService.checkUsernameDuplicate(username.trim());
        return ApiResponse.onSuccess(AuthSuccessCode.USERNAME_AVAILABLE, null);
    }

    @Override
    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmail(@RequestBody AuthReqDTO.EmailSendDTO dto) {
        authCommandService.sendCode(dto.email());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_SEND_SUCCESS, null);
    }

    @Override
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@RequestBody AuthReqDTO.EmailVerifyDTO dto) {
        authCommandService.verifyCode(dto.email(), dto.code());
        return ApiResponse.onSuccess(AuthSuccessCode.EMAIL_VERIFY_SUCCESS, null);
    }

    @Override
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResDTO.RegisterResDTO>> signUp(
            @RequestBody AuthReqDTO.RegisterReqDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(AuthSuccessCode.SIGNUP_SUCCESS, authCommandService.registerLocal(dto)));
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<AuthResDTO.LoginResultDTO> localLogin(
            @RequestBody AuthReqDTO.LocalLoginDTO dto,
            HttpServletResponse response
    ) {

        AuthResDTO.LoginResultDTO result = authCommandService.loginLocal(dto);

        // 리프레시 토큰은 쿠키에 저장
        cookieUtil.addCookie(
                response,
                "refreshToken",
                result.refreshToken(),
                (int) jwtUtil.getRefreshTokenExpiration() / 1000,
                "/auth/token/refresh"
        );
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_OK, result);
    }

    @Override
    @PostMapping("/token/refresh")
    public ApiResponse<AuthResDTO.LoginResultDTO> tokenReissue(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AuthResDTO.LoginResultDTO result = authCommandService.tokenReissue(accessToken, refreshToken);

        // 리프레시 토큰은 쿠키에 저장
        cookieUtil.addCookie(
                response,
                "refreshToken",
                result.refreshToken(),
                (int) jwtUtil.getRefreshTokenExpiration() / 1000,
                "/auth/token/refresh"
        );

        return ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUE_SUCCESS, result);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CurrentMember Long memberId,
            @RequestHeader("Authorization") String accessToken,
            HttpServletResponse response
    ) {
        authCommandService.logout(memberId, accessToken);
        cookieUtil.deleteCookie(response, "refreshToken", "/auth/token/refresh");

        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_OK, null);
    }
}
