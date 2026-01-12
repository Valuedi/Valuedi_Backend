package org.umc.valuedi.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.code.AuthSuccessCode;
import org.umc.valuedi.domain.auth.service.command.AuthCommandService;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService authCommandService;
    private final KakaoProperties kakaoProperties;

    @GetMapping("/oauth/kakao/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect(kakaoProperties.getKakaoAuthUrl());
    }

    @GetMapping("/oauth/kakao/callback")
    public ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @RequestParam("code")
            String code
    ) {
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_OK, authCommandService.loginKakao(code));
    }
}
