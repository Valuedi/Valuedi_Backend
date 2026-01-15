package org.umc.valuedi.domain.asset.connection.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.asset.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.asset.connection.service.ConnectionCommandService;
import org.umc.valuedi.domain.asset.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.domain.asset.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.apiPayload.code.GeneralSuccessCode;
import org.umc.valuedi.global.external.codef.exception.code.CodefSuccessCode;

import java.util.List;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionCommandService connectionCommandService;
    private final ConnectionQueryService connectionQueryService;

    @Operation(summary = "금융사 계정 연동", description = "은행 또는 카드사 계정을 연동합니다.")
    @PostMapping("/connections")
    public ApiResponse<Void> connect(
            // @CurrentMember Long memberId,
            @RequestBody ConnectionReqDTO.Connect request
    ) {
        Long memberId = 1L; // 임시
        connectionCommandService.connect(memberId, request);
        return ApiResponse.onSuccess(CodefSuccessCode.CODEF_CONNECTION_SUCCESS, null);
    }

    @Operation(summary = "모든 연동 목록 조회", description = "연동된 모든 금융사(은행+카드) 목록을 조회합니다.")
    @GetMapping("/connections")
    public ApiResponse<List<ConnectionResDTO.Connection>> getAllConnections(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<ConnectionResDTO.Connection> connections = connectionQueryService.getAllConnections(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, connections);
    }

    // TODO: DELETE /api/codef/connections/{connectionId}
}
