package org.umc.valuedi.domain.asset.connection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.valuedi.domain.asset.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.domain.asset.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

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
                                                  "id": 1,
                                                  "organization": "국민은행",
                                                  "type": "BANK",
                                                  "connectedAt": "2024-05-20T10:00:00"
                                                },
                                                {
                                                  "id": 2,
                                                  "organization": "현대카드",
                                                  "type": "CARD",
                                                  "connectedAt": "2024-05-22T09:00:00"
                                                }
                                              ]
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<ConnectionResDTO.Connection>> getAllConnections();
}