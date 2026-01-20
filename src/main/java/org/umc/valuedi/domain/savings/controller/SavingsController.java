package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.savings.dto.response.SavingsDetailResponseDTO;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponseDTO;
import org.umc.valuedi.domain.savings.service.SavingsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController implements SavingsControllerDocs {

    private final SavingsService savingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SavingsListResponseDTO>> findSavingsList(
            @PageableDefault(size = 10, sort = "korCoNm") Pageable pageable
    ) {
        SavingsListResponseDTO result = savingsService.getSavingsList(pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }

    @GetMapping("/{finPrdtCd}")
    public ResponseEntity<ApiResponse<SavingsDetailResponseDTO>> findSavingsDetail(
            @PathVariable String finPrdtCd
    ) {
        SavingsDetailResponseDTO result = savingsService.getSavingsDetail(finPrdtCd);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
