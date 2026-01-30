package org.umc.valuedi.domain.connection.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.connection.exception.code.ConnectionSuccessCode;
import org.umc.valuedi.domain.connection.service.ConnectionCommandService;
import org.umc.valuedi.domain.connection.service.ConnectionQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.external.codef.service.CodefAccountService;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController implements ConnectionControllerDocs {

    private final ConnectionCommandService connectionCommandService;
    private final ConnectionQueryService connectionQueryService;
    private final CodefAccountService codefAccountService;

    @PostMapping
    public ApiResponse<Void> connect(
            @CurrentMember Long memberId,
            @RequestBody ConnectionReqDTO.Connect request
    ) {
        codefAccountService.connectAccount(memberId, request);
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

    @PostMapping("/{connectionId}/sync")
    public ApiResponse<Void> sync(
            @CurrentMember Long memberId,
            @PathVariable Long connectionId
    ) {
        connectionCommandService.syncConnection(memberId, connectionId);
        return ApiResponse.onSuccess(ConnectionSuccessCode.CONNECTION_SYNC_REQUEST_SUCCESS, null);
    }
}
