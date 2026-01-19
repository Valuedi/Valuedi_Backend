package org.umc.valuedi.domain.asset.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
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

    @Operation(summary = "전체 계좌 목록 조회", description = "등록 최신순으로 정렬된 모든 은행 계좌 목록을 조회합니다.")
    @GetMapping("/banks/accounts")
    public ApiResponse<BankResDTO.BankAccountListDTO> getAllBankAccounts(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L;
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAllBankAccounts(memberId));
    }

    @Operation(summary = "은행별 계좌 목록 조회", description = "특정 은행에 연동된 계좌 목록을 조회합니다.")
    @GetMapping("/banks/{organization}/accounts")
    public ApiResponse<BankResDTO.BankAccountListDTO> getAccountsByBank(
            @PathVariable String organization
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L;
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getBankAccountsByOrganization(memberId, organization));
    }

    @Operation(summary = "연동 자산 개수 조회", description = "홈화면용 자산(계좌/카드) 개수를 조회합니다.")
    @GetMapping("/count")
    public ApiResponse<AssetResDTO.AssetSummaryCountDTO> getAssetCount(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L;
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAssetSummaryCount(memberId));
    }
}
