package org.umc.valuedi.domain.asset.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.exception.code.AssetSuccessCode;
import org.umc.valuedi.domain.asset.service.query.AssetQueryService;
import org.umc.valuedi.domain.connection.service.query.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
@Tag(name = "Asset", description = "자산 관련 조회 API")
public class AssetController implements AssetControllerDocs {

    private final ConnectionQueryService connectionQueryService;
    private final AssetQueryService assetQueryService;

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

    @GetMapping("/banks/{bankCode}")
    public ApiResponse<BankResDTO.BankAssetResponse> getAccountsByBank(
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

    @Override
    @GetMapping("/accounts/{accountId}/transactions")
    public ApiResponse<AssetResDTO.AssetTransactionResponse> getAccountTransactions(
            @PathVariable Long accountId,
            @CurrentMember Long memberId,
            @RequestParam(required = false) YearMonth yearMonth,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.onSuccess(
                AssetSuccessCode.ACCOUNT_TRANSACTIONS_FETCHED,
                assetQueryService.getAccountTransactions(memberId, accountId, yearMonth, date, page, size)
        );
    }

    @Override
    @GetMapping("/cards/{cardId}/transactions")
    public ApiResponse<AssetResDTO.AssetTransactionResponse> getCardTransactions(
            @PathVariable Long cardId,
            @CurrentMember Long memberId,
            @RequestParam(required = false) YearMonth yearMonth,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.onSuccess(
                AssetSuccessCode.CARD_TRANSACTIONS_FETCHED,
                assetQueryService.getCardTransactions(memberId, cardId, yearMonth, date, page, size)
        );
    }
}
