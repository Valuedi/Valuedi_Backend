package org.umc.valuedi.domain.savings.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@Tag(name = "Savings", description = "적금 추천 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "적금 추천 생성 API",
            description = """
                    로그인 사용자(JWT)의 금융 MBTI 결과를 바탕으로 Gemini가 즉시 추천 상품을 생성하고 저장합니다
                    
                    - 응답 속도는 Gemini API 호출을 포함하므로 약 3~7초 정도 소요될 수 있습니다
                    - 성공 시 추천된 상품 리스트와 Gemini의 추천 사유(rationale)가 반환됩니다.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "추천 상품 생성 및 저장 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "success",
                                                    summary = "추천 생성 성공 예시",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 15,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [
                                                          {
                                                            "korCoNm": "OO은행",
                                                            "finPrdtCd": "ABC123",
                                                            "finPrdtNm": "OO정기적금",
                                                            "rsrvType": "S",
                                                            "rsrvTypeNm": "정액적립식"
                                                          }
                                                        ],
                                                        "status": "SUCCESS",
                                                        "message": "사용자님의 MBTI 성향인 '도전적인 투자자'에 맞춰 높은 금리와 안정성을 동시에 잡을 수 있는 상품들을 추천해 드립니다..."
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "MBTI404_3",
                            description = "해당 MBTI 유형 정보가 존재하지 않습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "typeInfoNotFound",
                                                    summary = "MBTI 타입 정보 미존재",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MBTI404_3",
                                                      "message": "해당 MBTI 유형 정보가 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "GEMINI502_2",
                            description = "Gemini API 호출에 실패했습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "geminiFail",
                                                    summary = "Gemini 호출 실패",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "GEMINI502_2",
                                                      "message": "Gemini API 호출에 실패했습니다.",
                                                      "result": null
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ApiResponse<SavingsResponseDTO.SavingsListResponse> recommend(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "최신 추천 15개 조회 API",
            description = """
                    로그인 사용자(JWT)의 '현재 활성 MBTI 테스트' 기준으로
                    DB에 저장된 최신 추천 15개를 조회 (Gemini 호출 X)
                    
                    - 추천이 아직 생성되지 않았으면 status=PENDING, message에 안내 문구 포함
                    - 추천 생성에 실패한 경우 status=FAILED, message에 실패 안내 포함
                    """,
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
                                                    summary = "추천 15개 조회 성공",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 15,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [
                                                          {
                                                            "korCoNm": "OO은행",
                                                            "finPrdtCd": "ABC123",
                                                            "finPrdtNm": "OO정기적금",
                                                            "rsrvType": "S",
                                                            "rsrvTypeNm": "정액적립식"
                                                          }
                                                        ],
                                                        "status": "SUCCESS",
                                                        "message": null
                                                      }
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "pending",
                                                    summary = "추천 생성 중(PENDING)",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 0,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [],
                                                        "status": "PENDING",
                                                        "message": "추천 상품을 추천 중입니다. 잠시 후 다시 조회해 주세요."
                                                      }
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "noResultForType",
                                                    summary = "해당 적립유형 결과 없음(필터로 인해 비어있음)",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 0,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [],
                                                        "status": "SUCCESS",
                                                        "message": "조건에 맞는 추천 결과가 없습니다."
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "MBTI404_3",
                            description = "해당 MBTI 유형 정보가 존재하지 않습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "typeInfoNotFound",
                                                    summary = "MBTI 타입 정보 미존재",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "MBTI404_3",
                                                      "message": "해당 MBTI 유형 정보가 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ApiResponse<SavingsResponseDTO.SavingsListResponse> latest15(
            @Parameter(
                    description = "적립유형 필터 (S=정기적금, F=자유적금). 미입력 시 전체",
                    schema = @Schema(allowableValues = {"S", "F"}, example = "S")
            )
            @RequestParam(required = false) String rsrvType,
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "최신 추천 Top3 조회 API",
            description = """
                    로그인 사용자(JWT)의 '현재 활성 MBTI 테스트' 기준으로
                    DB에 저장된 최신 추천 Top3를 조회 (Gemini 호출 X)
                    
                    - 추천이 아직 생성되지 않았으면 status=PENDING, message에 안내 문구 포함
                    - 추천 생성에 실패한 경우 status=FAILED, message에 실패 안내 포함
                    """,
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
                                                    summary = "추천 Top3 조회 성공",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 3,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [
                                                          {
                                                            "korCoNm": "OO은행",
                                                            "finPrdtCd": "ABC123",
                                                            "finPrdtNm": "OO정기적금",
                                                            "rsrvType": "S",
                                                            "rsrvTypeNm": "정액적립식"
                                                          }
                                                        ],
                                                        "status": "SUCCESS",
                                                        "message": null
                                                      }
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "pending",
                                                    summary = "추천 생성 중(PENDING)",
                                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "totalCount": 0,
                                                        "maxPageNo": 1,
                                                        "nowPageNo": 1,
                                                        "products": [],
                                                        "status": "PENDING",
                                                        "message": "추천 상품을 추천 중입니다. 잠시 후 다시 조회해 주세요."
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ApiResponse<SavingsResponseDTO.SavingsListResponse> latestTop3(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "추천 상품 상세 조회 API",
            description = """
                    추천 목록에서 선택한 적금 상품(finPrdtCd)의 상세 정보를 조회
                    """,
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
                                                    summary = "성공 예시",
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
    ApiResponse<SavingsResponseDTO.SavingsDetailResponse> findSavingsDetail(
            @Parameter(description = "금융상품 코드(fin_prdt_cd)", example = "01012000200000000004")
            @PathVariable String finPrdtCd
    );
}
