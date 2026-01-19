package org.umc.valuedi.domain.terms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.service.MemberTermsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class MemberTermsController implements MemberTermsControllerDocs{

    private final MemberTermsService memberTermsService;

    @GetMapping("/member")
    public ResponseEntity<ApiResponse<TermsResponseDTO.GetMemberAgreements>> findMemberAgreements(
            @RequestParam Long memberId
    ) {
        TermsResponseDTO.GetMemberAgreements result = memberTermsService.getMemberAgreements(memberId);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }

    @PostMapping("/agree")
    public ResponseEntity<ApiResponse<Void>> agreeTerms(
            @RequestParam Long memberId,
            @RequestBody TermsRequestDTO.AgreeTermsRequest dto
    ) {
        memberTermsService.saveAgreeTerms(memberId, dto);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, null));
    }

}
