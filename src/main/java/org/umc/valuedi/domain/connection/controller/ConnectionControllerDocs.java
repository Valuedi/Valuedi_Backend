package org.umc.valuedi.domain.connection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.util.List;

@Tag(name = "Connection", description = "금융 연동 관련 API")
public interface ConnectionControllerDocs {

    @Operation(summary = "금융사 계정 연동 API", description = "은행 또는 카드사 계정을 새로 연동합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 금융사 연동 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "CODEF200_1",
                                              "message": "금융사 계정 연동에 성공하였습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "에러 - 잘못된 인증 정보 또는 요청",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "연동 실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CODEF400_1",
                                              "message": "잘못된 비밀번호이거나 인증 정보가 올바르지 않습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<Void> connect(
            @CurrentMember Long memberId,
            @RequestBody(description = "연동할 금융사 정보 및 인증 정보") ConnectionReqDTO.Connect request
    );

    @Operation(summary = "모든 연동 목록 조회 API", description = "연동된 모든 금융사(은행+카드) 목록을 통합하여 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 통합 연동 목록 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMON200",
                                              "message": "성공입니다.",
                                              "result": [
                                                {
                                                  "connectionId": 1,
                                                  "organization": "0020",
                                                  "type": "BK",
                                                  "connectedAt": "2024-05-20T10:00:00"
                                                },
                                                {
                                                  "connectionId": 2,
                                                  "organization": "0309",
                                                  "type": "CD",
                                                  "connectedAt": "2024-05-22T09:00:00"
                                                }
                                              ]
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<ConnectionResDTO.Connection>> getAllConnections(@CurrentMember Long memberId);

    @Operation(summary = "금융사 연동 해제 API", description = "특정 금융사(은행/카드사)와의 연동을 해제합니다. 연동 해제 시 해당 금융사에 속한 모든 계좌 및 카드가 비활성화됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 연동 해제 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "CONNECTION200_1",
                                              "message": "성공적으로 금융사 연동이 삭제되었습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "에러 - 연동 정보를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "CONNECTION404_1",
                                              "message": "해당 연동 정보를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<Void> disconnect(
            @CurrentMember Long memberId,
            @Parameter(description = "해제할 연동 ID (connectionId)") @PathVariable Long connectionId
    );
}
