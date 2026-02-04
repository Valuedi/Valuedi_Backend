package org.umc.valuedi.domain.connection.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.connection.dto.res.SyncLogResDTO;
import org.umc.valuedi.domain.connection.exception.code.ConnectionSuccessCode;
import org.umc.valuedi.domain.connection.service.command.ConnectionCommandService;
import org.umc.valuedi.domain.connection.service.query.ConnectionQueryService;
import org.umc.valuedi.domain.connection.service.query.SyncLogQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController implements ConnectionControllerDocs {

    private final ConnectionCommandService connectionCommandService;
    private final ConnectionQueryService connectionQueryService;
    private final SyncLogQueryService syncLogQueryService;

    @PostMapping
    public ApiResponse<Void> connect(
            @CurrentMember Long memberId,
            @RequestBody ConnectionReqDTO.Connect request
    ) {
        connectionCommandService.connect(memberId, request);
        return ApiResponse.onSuccess(ConnectionSuccessCode.CONNECTION_SUCCESS, null);
    }

    @GetMapping
    public ApiResponse<List<ConnectionResDTO.Connection>> getAllConnections(
            @CurrentMember Long memberId
    ) {
        List<ConnectionResDTO.Connection> connections = connectionQueryService.getAllConnections(memberId);
        return ApiResponse.onSuccess(ConnectionSuccessCode.CONNECTION_LIST_FETCH_SUCCESS, connections);
    }

    @DeleteMapping("/{connectionId}")
    public ApiResponse<Void> disconnect(
            @CurrentMember Long memberId,
            @PathVariable Long connectionId
    ) {
        connectionCommandService.disconnect(memberId, connectionId);
        return ApiResponse.onSuccess(ConnectionSuccessCode.CONNECTION_DELETE_SUCCESS, null);
    }

    @GetMapping("/sync/status")
    public ApiResponse<SyncLogResDTO.SyncLogResponseDTO> getSyncStatus(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(
                ConnectionSuccessCode.SYNC_STATUS_FETCH_SUCCESS,
                syncLogQueryService.getLatestSyncLog(memberId)
        );
    }
}
