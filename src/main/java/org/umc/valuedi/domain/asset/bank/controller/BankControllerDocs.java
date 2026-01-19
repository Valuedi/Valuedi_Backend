package org.umc.valuedi.domain.asset.bank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.valuedi.domain.asset.bank.dto.res.BankResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.util.List;

@Tag(name = "Bank", description = "은행 관련 API")
public interface BankControllerDocs {

    @Operation(summary = "연동된 은행 목록 조회 API", description = "현재 사용자가 연동한 은행 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 연동된 은행 목록 반환",
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
                                                  "bankName": "국민은행",
                                                  "bankCode": "004",
                                                  "connectedAt": "2024-05-20T10:00:00"
                                                },
                                                {
                                                  "bankName": "신한은행",
                                                  "bankCode": "088",
                                                  "connectedAt": "2024-05-21T15:30:00"
                                                }
                                              ]
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "에러 - 인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "인증 에러 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_1",
                                              "message": "로그인이 필요합니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "에러 - CODEF API 연동 오류",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "외부 API 연동 실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "BANK500_1",
                                              "message": "은행 연동 데이터를 가져오는 중 오류가 발생했습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<BankResDTO.BankConnection>> getBanks();
}