package org.umc.valuedi.domain.savings.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Savings", description = "적금 상품 조회 API")
public interface SavingsControllerDocs {

    @Operation(
            summary = "적금 상품 목록 조회 API",
            description = """
                적금 상품 목록을 조회하는 API
                (현재는 금감원 OpenAPI 기반 전체 목록을 반환하여, 추후 금융 MBTI/제미나이 연동 시 추천 로직이 적용될 예정입니다.)
                """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SavingsListResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 예시",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 54,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [
                                                          {
                                                            "korCoNm": "주식회사 케이뱅크",
                                                            "finPrdtCd": "01012000210000000000",
                                                            "finPrdtNm": "주거래우대 자유적금",
                                                            "rsrvType": "F",
                                                            "rsrvTypeNm": "자유적립식"
                                                          }
                                                        ]
                                                      }
                                                    }
                                            """
                                    )
                            )
                    ),

                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS400_1",
                            description = "금감원 API 요청 파라미터가 올바르지 않습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "파라미터 오류 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS400_1",
                                                      "message": "금감원 API 요청 파라미터가 올바르지 않습니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS401_1",
                            description = "금감원 인증키가 유효하지 않습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "인증키 오류 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS401_1",
                                                      "message": "금감원 인증키가 유효하지 않습니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS403_1",
                            description = "허용되지 않은 IP에서 요청했습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "IP 제한 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS403_1",
                                                      "message": "허용되지 않은 IP에서 요청했습니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS429_1",
                            description = "금감원 API 일일 호출 한도를 초과했습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "호출 한도 초과 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS429_1",
                                                      "message": "금감원 API 일일 호출 한도를 초과했습니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS502_1",
                            description = "금감원 API 내부 오류입니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "외부 API 내부 오류 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS502_1",
                                                      "message": "금감원 API 내부 오류입니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "FSS502_2",
                            description = "금감원 API 응답이 비어 있습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "빈 응답 예시",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "FSS502_2",
                                                      "message": "금감원 API 응답이 비어 있습니다.",
                                                      "result": null
                                                    }
                                            """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<ApiResponse<SavingsListResponse>> findSavingsList();
}
