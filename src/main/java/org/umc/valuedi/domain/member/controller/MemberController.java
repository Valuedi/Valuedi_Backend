package org.umc.valuedi.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.member.dto.req.MemberReqDTO;
import org.umc.valuedi.domain.member.dto.res.MemberResDTO;
import org.umc.valuedi.domain.member.exception.code.MemberSuccessCode;
import org.umc.valuedi.domain.member.service.command.MemberCommandService;
import org.umc.valuedi.domain.member.service.query.MemberQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @Override
    @GetMapping("/me")
    public ApiResponse<MemberResDTO.MemberInfoDTO> getMemberInfo(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_INFO_GET_SUCCESS, memberQueryService.getMemberInfo(memberId));
    }

    @Override
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(
            @CurrentMember Long memberId,
            @RequestHeader("Authorization") String accessToken,
            @RequestBody MemberReqDTO.MemberWithdrawDTO memberWithdrawDTO
            ) {
        memberCommandService.withdraw(memberId, accessToken, memberWithdrawDTO.reason());
        return ApiResponse.onSuccess(MemberSuccessCode.WITHDRAWAL_SUCCESS, null);
    }
}
