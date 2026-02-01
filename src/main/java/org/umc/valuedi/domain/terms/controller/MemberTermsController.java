package org.umc.valuedi.domain.terms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.exception.code.TermsSuccessCode;
import org.umc.valuedi.domain.terms.service.MemberTermsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class MemberTermsController implements MemberTermsControllerDocs{

    private final MemberTermsService memberTermsService;

    @Override
    @GetMapping("/member")
    public ApiResponse<TermsResponseDTO.GetMemberAgreements> findMemberAgreements(
            @CurrentMember Long memberId
    ) {
        TermsResponseDTO.GetMemberAgreements result = memberTermsService.getMemberAgreements(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Override
    @PostMapping("/agree")
    public ApiResponse<Void> agreeTerms(
            @CurrentMember Long memberId,
            @RequestBody TermsRequestDTO.AgreeTermsRequest dto
    ) {
        memberTermsService.updateMemberTerms(memberId, dto.agreements());
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_AGREE_SUCCESS, null);
    }

}
