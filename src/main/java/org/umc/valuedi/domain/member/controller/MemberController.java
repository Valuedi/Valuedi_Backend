package org.umc.valuedi.domain.member.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.member.exception.code.MemberSuccessCode;
import org.umc.valuedi.domain.member.service.MemberService;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@RestController
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/auth/check-username")
    public ApiResponse<Boolean> checkUsername(
            @RequestParam(name = "username")
            @NotBlank(message = "아이디를 입력해주세요.")
            String username
    ) {
            memberService.checkUsernameDuplicate(username);
            return ApiResponse.onSuccess(MemberSuccessCode.USERNAME_CHECK_OK, null);
    }
}
