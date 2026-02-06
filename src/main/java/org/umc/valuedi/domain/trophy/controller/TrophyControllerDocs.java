package org.umc.valuedi.domain.trophy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import org.umc.valuedi.domain.trophy.dto.response.TrophyMetaResponse;
import org.umc.valuedi.domain.trophy.dto.response.TrophyResponse;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.util.List;

@Tag(name = "Trophy API", description = "트로피 관련 API")
public interface TrophyControllerDocs {

    @Operation(summary = "전체 트로피 목록 조회", description = "서비스에 등록된 모든 트로피의 메타 정보(이름, 설명 등)를 조회합니다.")
    ApiResponse<List<TrophyMetaResponse>> getAllTrophies();

    @Operation(summary = "내 트로피 현황 조회", description = "로그인한 회원의 기간별 트로피 획득 현황(스냅샷)을 조회합니다.")
    ApiResponse<List<TrophyResponse>> getMyTrophies(
            @Parameter(description = "조회 기간 타입 (DAILY, MONTHLY, LAST_30_DAYS)", example = "MONTHLY")
            @RequestParam(name = "periodType", defaultValue = "MONTHLY") PeriodType periodType,

            @Parameter(description = "조회 기간 키 (예: '2025-12', '2025-12-27')", example = "2025-12")
            @RequestParam(name = "periodKey") String periodKey,

            @CurrentMember Long memberId
            );
}
