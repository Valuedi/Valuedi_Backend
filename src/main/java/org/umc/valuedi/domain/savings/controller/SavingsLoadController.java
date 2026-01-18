package org.umc.valuedi.domain.savings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.savings.service.SavingsLoadService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

@RestController
@RequestMapping("/api/admin/savings")
@RequiredArgsConstructor
public class SavingsLoadController implements SavingsLoadControllerDocs {

    private final SavingsLoadService savingsLoadService;

    // FSS에서 적금 상품을 가져와 DB에 적재
    @PostMapping("/load")
    public ResponseEntity<ApiResponse<Integer>> loadSavings(
            @RequestParam(defaultValue = "1") Integer pageNo
    ) {
        int result = savingsLoadService.loadAndUpsert(pageNo);
        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, result));
    }
}
