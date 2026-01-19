package org.umc.valuedi.domain.asset.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.umc.valuedi.domain.asset.card.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

import java.util.List;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class CardController implements CardControllerDocs{

    private final ConnectionQueryService connectionQueryService;

    @GetMapping("/cards")
    public ApiResponse<List<CardResDTO.CardConnection>> getCards(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<CardResDTO.CardConnection> cards = connectionQueryService.getConnectedCards(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, cards);
    }
}
