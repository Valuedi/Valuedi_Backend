package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.service.SavingsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController implements SavingsControllerDocs {

    private final SavingsService savingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SavingsResponseDTO.SavingsListResponse>> findSavingsList(
            @PageableDefault(size = 10, sort = "korCoNm") Pageable pageable
    ) {
        SavingsResponseDTO.SavingsListResponse result = savingsService.getSavingsList(pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }

    @GetMapping("/{finPrdtCd}")
    public ResponseEntity<ApiResponse<SavingsResponseDTO.SavingsDetailResponse>> findSavingsDetail(
            @PathVariable String finPrdtCd
    ) {
        SavingsResponseDTO.SavingsDetailResponse result = savingsService.getSavingsDetail(finPrdtCd);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
