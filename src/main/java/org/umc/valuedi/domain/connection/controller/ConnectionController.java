package org.umc.valuedi.domain.connection.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.connection.exception.code.ConnectionSuccessCode;
import org.umc.valuedi.domain.connection.service.ConnectionCommandService;
import org.umc.valuedi.domain.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController implements ConnectionControllerDocs {

    private final ConnectionCommandService connectionCommandService;
    private final ConnectionQueryService connectionQueryService;

    @PostMapping
    public ApiResponse<Void> connect(
            // @CurrentMember Long memberId,
            @RequestBody ConnectionReqDTO.Connect request
    ) {
        Long memberId = 1L; // 임시
        connectionCommandService.connect(memberId, request);
        return ApiResponse.onSuccess(CodefSuccessCode.CODEF_CONNECTION_SUCCESS, null);
    }

    @GetMapping
    public ApiResponse<List<ConnectionResDTO.Connection>> getAllConnections(
            // @CurrentMember Long memberId
    ) {
        Long memberId = 1L; // 임시
        List<ConnectionResDTO.Connection> connections = connectionQueryService.getAllConnections(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, connections);
    }

    @DeleteMapping("/{connectionId}")
    public ApiResponse<Void> disconnect(
            // @CurrentMember Long memberId,
            @PathVariable Long connectionId
    ) {
        Long memberId = 1L;
        connectionCommandService.disconnect(memberId, connectionId);
        return ApiResponse.onSuccess(ConnectionSuccessCode.CONNECTION_DELETE_SUCCESS, null);
    }
}
