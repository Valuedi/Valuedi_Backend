package org.umc.valuedi.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.member.dto.res.MemberResDTO;
import org.umc.valuedi.domain.member.exception.code.MemberSuccessCode;
import org.umc.valuedi.domain.member.service.query.MemberQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {
    private final MemberQueryService memberQueryService;

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResDTO.MemberInfoDTO> getMemberInfo(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_INFO_GET_SUCCESS, memberQueryService.getMemberInfo(memberId));
    }
}
