package org.umc.valuedi.domain.asset.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.service.AssetQueryService;
import org.umc.valuedi.domain.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
@Tag(name = "Asset", description = "자산 관련 조회 API")
public class AssetController implements AssetControllerDocs {

    private final ConnectionQueryService connectionQueryService;
    private final AssetQueryService assetQueryService;


    @GetMapping("/cards")
    public ApiResponse<List<CardResDTO.CardConnection>> getCards(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<CardResDTO.CardConnection> cards = connectionQueryService.getConnectedCards(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, cards);
    }

    @GetMapping("/banks")
    public ApiResponse<List<BankResDTO.BankConnection>> getBanks( // TODO: List 객체 래핑으로 변경
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<BankResDTO.BankConnection> banks = connectionQueryService.getConnectedBanks(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, banks);
    }

    @Operation(summary = "연동된 전체 계좌 목록 조회", description = "등록 최신순으로 정렬된 모든 은행 계좌 목록을 조회합니다.")
    @GetMapping("/banks/accounts")
    public ApiResponse<BankResDTO.BankAccountListDTO> getAllBankAccounts(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L;
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAllBankAccounts(memberId));
    }
}
