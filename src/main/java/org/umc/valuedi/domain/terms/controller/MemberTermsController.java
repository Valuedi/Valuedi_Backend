package org.umc.valuedi.domain.terms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
