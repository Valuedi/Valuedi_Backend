package org.umc.valuedi.domain.savings.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Savings-Admin", description = "적금 상품 적재(개발/운영) API")
public interface SavingsLoadControllerDocs {

    @Operation(
            summary = "적금 상품 DB 적재 API",
            description = "금감원(FSS) 적금 상품 목록을 조회해 DB에 저장합니다. (개발/운영용)",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = @ExampleObject(
                                            name = "success",
                                            summary = "성공 예시(적재된 상품 개수 반환)",
                                            value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMON200",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "result": 50
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
                                            name = "invalidParameter",
                                            summary = "INVALID_PARAMETER 예시",
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
                            description = "금감원 인증키가 올바르지 않습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "invalidAuth",
                                            summary = "INVALID_AUTH 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "FSS401_1",
                                              "message": "금감원 인증키가 올바르지 않습니다.",
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
                                            name = "notAllowedIp",
                                            summary = "NOT_ALLOWED_IP 예시",
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
                                            name = "dailyLimitExceeded",
                                            summary = "DAILY_LIMIT_EXCEEDED 예시",
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
                            description = "금감원 API 서버 오류가 발생했습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "fssInternalError",
                                            summary = "FSS_INTERNAL_ERROR 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "FSS502_1",
                                              "message": "금감원 API 서버 오류가 발생했습니다.",
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
                                            name = "emptyResponse",
                                            summary = "EMPTY_RESPONSE 예시",
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
    ResponseEntity<org.umc.valuedi.global.apiPayload.ApiResponse<Integer>> loadSavings(
            @RequestParam(defaultValue = "1") Integer pageNo
    );
}
