package org.umc.valuedi.domain.trophy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.trophy.dto.response.TrophyMetaResponse;
import org.umc.valuedi.domain.trophy.dto.response.TrophyResponse;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.domain.trophy.service.TrophyService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrophyController implements TrophyControllerDocs{

    private final TrophyService trophyService;

    @GetMapping("/trophies")
    public ApiResponse<List<TrophyMetaResponse>> getAllTrophies() {
        List<TrophyMetaResponse> response = trophyService.getAllTrophies();
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @GetMapping("/members/me/trophies")
    public ApiResponse<List<TrophyResponse>> getMyTrophies(
            @RequestParam(name = "periodType", defaultValue = "MONTHLY") PeriodType periodType,
            @RequestParam(name = "periodKey") String periodKey
//            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
//        Long memberId = Long.parseLong(userDetails.getUsername());
        Long memberId = 1L;

        List<TrophyResponse> response = trophyService.getMyTrophies(memberId, periodType, periodKey);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
