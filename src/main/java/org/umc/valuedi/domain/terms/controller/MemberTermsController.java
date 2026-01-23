package org.umc.valuedi.domain.terms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.exception.code.TermsSuccessCode;
import org.umc.valuedi.domain.terms.service.MemberTermsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class MemberTermsController implements MemberTermsControllerDocs{

    private final MemberTermsService memberTermsService;

    @Override
    @GetMapping("/member")
    public ApiResponse<TermsResponseDTO.GetMemberAgreements> findMemberAgreements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TermsResponseDTO.GetMemberAgreements result = memberTermsService.getMemberAgreements(userDetails.getMemberId());
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    @Override
    @PostMapping("/agree")
    public ApiResponse<Void> agreeTerms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TermsRequestDTO.AgreeTermsRequest dto
    ) {
        memberTermsService.updateMemberTerms(userDetails.getMemberId(), dto.agreements());
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_AGREE_SUCCESS, null);
    }

}
