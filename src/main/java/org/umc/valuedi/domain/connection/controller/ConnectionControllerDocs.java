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
import org.umc.valuedi.domain.connection.dto.res.SyncLogResDTO;
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

    @Operation(
            summary = "전체 자산 새로고침(동기화) 요청 API",
            description = """
                    사용자가 연동한 모든 금융사의 최신 거래내역 수집을 **백그라운드에서 시작**하도록 요청합니다.
                    - **즉시 응답**: API는 동기화 작업을 백그라운드로 넘기고 즉시 '요청 성공' 응답을 반환합니다.
                    - **결과 확인**: 실제 동기화 결과는 잠시 후 자산 관련 다른 API를 통해 확인해야 합니다.
                    - **10분 쿨타임**: 마지막 동기화 시간으로부터 10분 이내에는 재호출할 수 없습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 동기화 작업이 성공적으로 시작됨",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "ASSET200_1",
                                              "message": "자산 동기화 요청이 성공적으로 접수되었습니다. 잠시 후 데이터를 확인해주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "실패 - 10분 쿨타임 제한",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "쿨타임 실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ASSET429_1",
                                              "message": "자산 동기화는 10분에 한 번만 요청할 수 있습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> refreshAssetSync(@Parameter(hidden = true) @CurrentMember Long memberId);

    @Operation(
            summary = "자산 동기화 상태 조회 API",
            description = """
                    최근 요청한 자산 동기화 작업의 진행 상태를 조회합니다. \s
                    전체 동기화 API 호출 후, 백그라운드 작업이 완료되었는지 확인하기 위한 용도입니다. \s
                    상태값 설명: \s
                    - **IN_PROGRESS**: 현재 금융사로부터 데이터를 가져오는 중 \s
                    - **SUCCESS**: 모든 데이터 수집 및 가계부 반영 완료 \s
                    - **FAILED**: 동기화 중 오류 발생 (errorMessage 필드에 상세 내용 포함)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 상태 정보 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "1. 진행 중 (IN_PROGRESS)",
                                            summary = "동기화가 아직 진행 중인 경우",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "SYNC200_1",
                                                      "message": "동기화 상태를 성공적으로 조회했습니다.",
                                                      "result": {
                                                        "syncLogId": 12,
                                                        "syncStatus": "IN_PROGRESS",
                                                        "syncType": "ALL",
                                                        "errorMessage": null,
                                                        "updatedAt": "2024-05-24T15:00:00"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "2. 성공 (SUCCESS)",
                                            summary = "동기화가 완료되어 최신 데이터가 반영된 경우",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "SYNC200_1",
                                                      "message": "동기화 상태를 성공적으로 조회했습니다.",
                                                      "result": {
                                                        "syncLogId": 12,
                                                        "syncStatus": "SUCCESS",
                                                        "syncType": "ALL",
                                                        "errorMessage": null,
                                                        "updatedAt": "2024-05-24T15:02:30"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "3. 실패 (FAILED)",
                                            summary = "금융사 인증 오류 또는 네트워크 문제로 실패한 경우",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "SYNC200_1",
                                                      "message": "동기화 상태를 성공적으로 조회했습니다.",
                                                      "result": {
                                                        "syncLogId": 12,
                                                        "syncStatus": "FAILED",
                                                        "syncType": "ALL",
                                                        "errorMessage": "금융사 서버 응답 지연 (Timeout)",
                                                        "updatedAt": "2024-05-24T15:01:00"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "에러 - 동기화 기록이 전혀 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "기록 없음 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "SYNC404_1",
                                              "message": "해당 동기화 로그를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    ApiResponse<SyncLogResDTO.SyncLogResponseDTO> getSyncStatus(@CurrentMember Long memberId);

}
