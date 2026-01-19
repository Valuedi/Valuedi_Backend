package org.umc.valuedi.domain.terms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.service.TermsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController implements TermsControllerDocs {

    private final TermsService termsService;

    @GetMapping
    public ResponseEntity<ApiResponse<TermsResponseDTO.GetTermsList>> findTermsList() {
        TermsResponseDTO.GetTermsList result = termsService.getTermsList();
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
