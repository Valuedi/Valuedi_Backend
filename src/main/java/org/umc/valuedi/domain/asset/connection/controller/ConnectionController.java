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
public class ConnectionController implements ConnectionControllerDocs {

    private final ConnectionCommandService connectionCommandService;
    private final ConnectionQueryService connectionQueryService;

    @PostMapping("/connections")
    public ApiResponse<Void> connect(
            // @CurrentMember Long memberId,
            @RequestBody ConnectionReqDTO.Connect request
    ) {
        Long memberId = 1L; // 임시
        connectionCommandService.connect(memberId, request);
        return ApiResponse.onSuccess(CodefSuccessCode.CODEF_CONNECTION_SUCCESS, null);
    }

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
