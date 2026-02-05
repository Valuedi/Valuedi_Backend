package org.umc.valuedi.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.auth.exception.code.AuthSuccessCode;
import org.umc.valuedi.domain.auth.service.command.AuthCommandService;
import org.umc.valuedi.domain.auth.service.query.AuthQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;
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

    @Override
    @GetMapping("/oauth/kakao/login")
    public ApiResponse<AuthResDTO.LoginUrlDTO> kakaoLogin() {
        String state = UUID.randomUUID().toString();
        String loginUrl = kakaoProperties.getKakaoAuthUrl(state);
        return ApiResponse.onSuccess(AuthSuccessCode.KAKAO_AUTH_URL_SUCCESS, AuthConverter.toLoginUrlDTO(loginUrl, state));
    }

    @Override
    @GetMapping("/oauth/kakao/callback")
    public ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam("originalState") String originalState
    ) {
        if (originalState == null || !originalState.equals(state)) {
            throw new AuthException(AuthErrorCode.INVALID_STATE);
        }

        AuthResDTO.LoginResultDTO result = authCommandService.loginKakao(code);
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
            @RequestBody AuthReqDTO.LocalLoginDTO dto
    ) {

        AuthResDTO.LoginResultDTO result = authCommandService.loginLocal(dto);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_OK, result);
    }

    @Override
    @PostMapping("/token/refresh")
    public ApiResponse<AuthResDTO.LoginResultDTO> tokenReissue(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam String refreshToken
    ) {
        AuthResDTO.LoginResultDTO result = authCommandService.tokenReissue(accessToken, refreshToken);
        return ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REISSUE_SUCCESS, result);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CurrentMember Long memberId,
            @RequestHeader("Authorization") String accessToken
    ) {
        authCommandService.logout(memberId, accessToken);

        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_OK, null);
    }

    @Override
    @GetMapping("/status")
    public ApiResponse<AuthResDTO.AuthStatusDTO> getAuthStatus(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(AuthSuccessCode.AUTH_STATUS_GET_SUCCESS, authQueryService.getAuthStatus(memberId));
    }
}
