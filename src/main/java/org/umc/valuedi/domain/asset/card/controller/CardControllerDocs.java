package org.umc.valuedi.domain.asset.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.valuedi.domain.asset.card.dto.res.CardResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.util.List;

@Tag(name = "Card", description = "카드 관련 API")
public interface CardControllerDocs {

    @Operation(summary = "연동된 카드 목록 조회 API", description = "현재 사용자가 연동한 카드 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 연동된 카드 목록 반환",
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
                                                  "cardName": "현대카드 M Edition3",
                                                  "cardNumber": "4321-****-****-1234",
                                                  "cardType": "CREDIT",
                                                  "connectedAt": "2024-05-22T09:00:00"
                                                },
                                                {
                                                  "cardName": "신한 Deep Dream",
                                                  "cardNumber": "5336-****-****-5678",
                                                  "cardType": "CHECK",
                                                  "connectedAt": "2024-05-23T11:20:00"
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
                    description = "에러 - 서버 내부 오류 (데이터 조회 실패)",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 에러 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON500",
                                              "message": "서버 에러, 관리자에게 문의 바랍니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<CardResDTO.CardConnection>> getCards();
}