package org.umc.valuedi.domain.asset.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codef/assets")
public class AssetController implements AssetControllerDocs {

    private final ConnectionQueryService connectionQueryService;


    @GetMapping("/cards")
    public ApiResponse<List<CardResDTO.CardConnection>> getCards(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<CardResDTO.CardConnection> cards = connectionQueryService.getConnectedCards(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, cards);
    }

    @GetMapping("/banks")
    public ApiResponse<List<BankResDTO.BankConnection>> getBanks(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<BankResDTO.BankConnection> banks = connectionQueryService.getConnectedBanks(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, banks);
    }
}
