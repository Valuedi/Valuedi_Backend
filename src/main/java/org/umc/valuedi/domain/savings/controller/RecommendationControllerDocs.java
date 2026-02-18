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
                    로그인 사용자(JWT)의 현재 MBTI를 바탕으로 Gemini 추천을 생성하고 DB를 갱신합니다.
                    MBTI 검사 완료 후 이 API를 호출하여 맞춤 추천을 받을 수 있습니다.

                    - 응답 속도는 Gemini API 호출을 포함하므로 약 10~20초 정도 소요될 수 있습니다
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
                                                        ]
                                                      }
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "SAVINGS500_1",
                            description = "AI 추천 생성 실패 (Gemini 호출 실패 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "fail",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "SAVINGS500_1",
                                              "message": "AI 추천 생성에 실패했습니다. 다시 시도해 주세요.",
                                              "result": null
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    ApiResponse<SavingsResponseDTO.SavingsListResponse> recommend(
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "최신 추천 10개 조회 API",
            description = """
                    로그인 사용자(JWT)의 '현재 활성 MBTI 테스트' 기준으로
                    DB에 저장된 최신 추천 10개를 조회 (Gemini 호출 X)
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
                                                    summary = "추천 10개 조회 성공",
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
                                                        ]
                                                      }
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "noHistory",
                                                    summary = "추천 내역 없음 (MBTI 검사 미실행 등)",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SAVINGS404_2",
                                                      "message": "아직 추천받은 내역이 없습니다. 먼저 상품 추천을 진행해 주세요.",
                                                      "result": null
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ApiResponse<SavingsResponseDTO.SavingsListResponse> latest10(
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
                                                        ]
                                                      }
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "noHistory",
                                                    summary = "추천 내역 없음 (SAVINGS404_2)",
                                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "SAVINGS404_2",
                                                      "message": "아직 추천받은 내역이 없습니다. 먼저 상품 추천을 진행해 주세요.",
                                                      "result": null
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
