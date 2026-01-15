package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.domain.savings.service.SavingsService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsController implements SavingsControllerDocs {

    private final SavingsService savingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<SavingsListResponse>> findSavingsList() {
        SavingsListResponse result = savingsService.getSavingsList();
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
