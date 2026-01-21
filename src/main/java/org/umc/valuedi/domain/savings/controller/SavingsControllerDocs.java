package org.umc.valuedi.domain.savings.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Savings", description = "적금 상품 API")
public interface SavingsControllerDocs {

    @Operation(
            summary = "적금 상품 목록 조회 API",
            description = "적금 상품 목록을 조회하는 API",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "success",
                                                    summary = "성공 응답 예시",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 2,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [
                                                          {
                                                            "korCoNm": "OO은행",
                                                            "finPrdtCd": "ABC123",
                                                            "finPrdtNm": "OO정기적금",
                                                            "rsrvType": "S",
                                                            "rsrvTypeNm": "정액적립식"
                                                          },
                                                          {
                                                            "korCoNm": "XX저축은행",
                                                            "finPrdtCd": "DEF456",
                                                            "finPrdtNm": "XX자유적금",
                                                            "rsrvType": "F",
                                                            "rsrvTypeNm": "자유적립식"
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ResponseEntity<ApiResponse<SavingsResponseDTO.SavingsListResponse>> findSavingsList(
            @Parameter(description = "페이지 정보(page, size, sort)")
            @PageableDefault(size = 10, sort = "korCoNm") Pageable pageable
    );


    @Operation(
            summary = "적금 상품 상세 조회 API",
            description = "적금 상품 목록을 상세 조회하는 API",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "success",
                                                    summary = "성공 응답 예시",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "product": {
                                                          "korCoNm": "OO은행",
                                                          "finPrdtCd": "ABC123",
                                                          "finPrdtNm": "OO정기적금",
                                                          "joinWay": "영업점/인터넷/모바일",
                                                          "mtrtInt": "만기 후 이자율 안내",
                                                          "spclCnd": "우대조건 안내",
                                                          "joinDeny": "1",
                                                          "joinMember": "개인",
                                                          "etcNote": "유의사항",
                                                          "maxLimit": "500000",
                                                          "options": [
                                                            {
                                                              "intrRateType": "S",
                                                              "intrRateTypeNm": "단리",
                                                              "rsrvType": "S",
                                                              "rsrvTypeNm": "정액적립식",
                                                              "saveTrm": "12",
                                                              "intrRate": 3.2,
                                                              "intrRate2": 3.8
                                                            }
                                                          ]
                                                        }
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "SAVINGS404_1",
                            description = "해당 적금 상품을 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "notFound",
                                                    summary = "상품 미존재 예시",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SAVINGS404_1",
                                                      "message": "해당 적금 상품을 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ResponseEntity<ApiResponse<SavingsResponseDTO.SavingsDetailResponse>> findSavingsDetail(
            @Parameter(description = "금융상품 코드(fin_prdt_cd)")
            @PathVariable String finPrdtCd
    );
}

