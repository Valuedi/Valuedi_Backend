package org.umc.valuedi.domain.asset.bank.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.bank.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

import java.util.List;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class BankController {

    private final ConnectionQueryService connectionQueryService;

    @Operation(summary = "연동된 은행 목록 조회", description = "연동된 은행 목록을 조회합니다.")
    @GetMapping("/banks/connections")
    public ApiResponse<List<BankResDTO.BankConnection>> getBanks(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<BankResDTO.BankConnection> banks = connectionQueryService.getConnectedBanks(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, banks);
    }
}
