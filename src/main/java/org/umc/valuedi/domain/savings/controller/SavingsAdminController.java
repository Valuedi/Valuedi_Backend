package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.service.SavingsAdminService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/admin/savings")
@RequiredArgsConstructor
public class SavingsAdminController implements SavingsAdminControllerDocs {

    private final SavingsAdminService savingsAdminService;

    // FSS에서 적금 상품을 가져와 DB에 적재
    @PostMapping("/load")
    public ResponseEntity<ApiResponse<Integer>> loadSavings(
            @RequestParam(defaultValue = "1") Integer pageNo
    ) {
        int result = savingsAdminService.loadAndUpsert(pageNo);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }

    // 적재된 적금 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<SavingsResponseDTO.SavingsListResponse>> findSavingsList(
            @PageableDefault(size = 10, sort = "korCoNm") Pageable pageable
    ) {
        SavingsResponseDTO.SavingsListResponse result = savingsAdminService.getSavingsList(pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
