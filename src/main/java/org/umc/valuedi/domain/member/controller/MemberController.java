package org.umc.valuedi.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.member.exception.code.MemberSuccessCode;
import org.umc.valuedi.domain.member.service.query.MemberQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@RestController
@RequiredArgsConstructor
@Validated
public class MemberController implements MemberControllerDocs {
    private final MemberQueryService memberService;

    @Override
    @GetMapping("/auth/check-username")
    public ApiResponse<Void> checkUsername(
            @RequestParam(name = "username")
            String username
    ) {
            memberService.checkUsernameDuplicate(username.trim());
            return ApiResponse.onSuccess(MemberSuccessCode.USERNAME_CHECK_OK, null);
    }
}
