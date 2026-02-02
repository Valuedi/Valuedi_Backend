package org.umc.valuedi.domain.asset.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.exception.code.AssetSuccessCode;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.service.query.AssetQueryService;
import org.umc.valuedi.domain.asset.service.command.AssetSyncFacadeService;
import org.umc.valuedi.domain.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
@Tag(name = "Asset", description = "자산 관련 조회 API")
public class AssetController implements AssetControllerDocs {

    private final ConnectionQueryService connectionQueryService;
    private final AssetQueryService assetQueryService;
    private final AssetSyncFacadeService assetSyncFacadeService;


    @GetMapping("/cards")
    public ApiResponse<CardResDTO.CardListDTO> getCards(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAllCards(memberId));
    }

    @GetMapping("/cardIssuers")
    public ApiResponse<List<CardResDTO.CardIssuerConnection>> getCardIssuers(
            @CurrentMember Long memberId
    ) {
        List<CardResDTO.CardIssuerConnection> cardIssuers = connectionQueryService.getConnectedCardIssuers(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, cardIssuers);
    }

    @GetMapping("/cardIssuers/{issuerCode}/cards")
    public ApiResponse<CardResDTO.CardListDTO> getCardsByIssuer(
            @PathVariable String issuerCode,
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getCardsByIssuer(memberId, issuerCode));
    }

    @GetMapping("/banks")
    public ApiResponse<List<BankResDTO.BankConnection>> getBanks( // TODO: List 객체 래핑으로 변경
            @CurrentMember Long memberId
    ) {
        List<BankResDTO.BankConnection> banks = connectionQueryService.getConnectedBanks(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, banks);
    }

    @GetMapping("/accounts")
    public ApiResponse<BankResDTO.BankAccountListDTO> getAllBankAccounts(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAllBankAccounts(memberId));
    }

    @GetMapping("/banks/{bankCode}/accounts")
    public ApiResponse<BankResDTO.BankAccountListDTO> getAccountsByBank(
            @PathVariable String bankCode,
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getBankAccountsByOrganization(memberId, bankCode));
    }

    @GetMapping("/summary")
    public ApiResponse<AssetResDTO.AssetSummaryCountDTO> getAssetCount(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, assetQueryService.getAssetSummaryCount(memberId));
    }

    @PostMapping("/sync/refresh")
    public ApiResponse<Void> refreshAssetSync(
            @CurrentMember Long memberId
    ) {
        assetSyncFacadeService.refreshAssetSync(memberId);
        return ApiResponse.onSuccess(AssetSuccessCode.SYNC_REQUEST_SUCCESS, null);
    }
}
