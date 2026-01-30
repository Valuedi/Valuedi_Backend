package org.umc.valuedi.domain.asset.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.util.List;

@Tag(name = "Asset", description = "자산(계좌/카드) 관련 API")
public interface AssetControllerDocs {

    @Operation(summary = "연동된 전체 카드 목록 조회 API", description = "현재 사용자가 연동한 모든 카드를 조회합니다.")
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
                                              "result": {
                                                "cardList": [
                                                  {
                                                    "cardName": "현대카드 M Edition3",
                                                    "cardNoMasked": "4321-****-****-1234",
                                                    "cardType": "CREDIT",
                                                    "organization": "0302",
                                                    "createdAt": "2024-05-22T09:00:00"
                                                  },
                                                  {
                                                    "cardName": "신한 Deep Dream",
                                                    "cardNoMasked": "5336-****-****-5678",
                                                    "cardType": "CHECK",
                                                    "organization": "0304",
                                                    "createdAt": "2024-05-23T11:20:00"
                                                  }
                                                ],
                                                "totalCount": 2
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<CardResDTO.CardListDTO> getCards();

    @Operation(summary = "연동된 카드사 목록 조회 API", description = "현재 사용자가 연동한 카드사 리스트를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 연동된 카드사 목록 반환",
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
                                                  "organizationCode": "0301",
                                                  "organizationName": "KB국민카드",
                                                  "connectedAt": "2024-05-20T10:00:00",
                                                  "status": "ACTIVE"
                                                },
                                                {
                                                  "id": 2,
                                                  "organizationCode": "0302",
                                                  "organizationName": "현대카드",
                                                  "connectedAt": "2024-05-21T15:30:00",
                                                  "status": "ACTIVE"
                                                }
                                              ]
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<CardResDTO.CardIssuerConnection>> getCardIssuers();

    @Operation(summary = "카드사별 카드 목록 조회", description = "특정 카드사에 연동된 카드 목록을 조회합니다.")
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
                                              "result": {
                                                "cardList": [
                                                  {
                                                    "cardName": "현대카드 M Edition3",
                                                    "cardNoMasked": "4321-****-****-1234",
                                                    "cardType": "CREDIT",
                                                    "organization": "0302",
                                                    "createdAt": "2024-05-22T09:00:00"
                                                  }
                                                ],
                                                "totalCount": 1
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<CardResDTO.CardListDTO> getCardsByIssuer(@Parameter(description = "카드사 코드 (예: 0301)") String issuerCode);

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
                                                  "id": 1,
                                                  "organizationCode": "0004",
                                                  "organizationName": "KB국민은행",
                                                  "connectedAt": "2024-05-20T10:00:00",
                                                  "status": "ACTIVE"
                                                },
                                                {
                                                  "id": 2,
                                                  "organizationCode": "0088",
                                                  "organizationName": "신한은행",
                                                  "connectedAt": "2024-05-21T15:30:00",
                                                  "status": "ACTIVE"
                                                }
                                              ]
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<List<BankResDTO.BankConnection>> getBanks();

    @Operation(summary = "전체 계좌 목록 조회", description = "등록 최신순으로 정렬된 모든 은행 계좌 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 전체 계좌 목록 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMON200",
                                              "message": "성공입니다.",
                                              "result": {
                                                "accountList": [
                                                  {
                                                    "accountName": "KB나라사랑우대통장",
                                                    "balanceAmount": 150000,
                                                    "organization": "0004",
                                                    "createdAt": "2024-05-20T10:00:00"
                                                  }
                                                ],
                                                "totalCount": 1
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<BankResDTO.BankAccountListDTO> getAllBankAccounts();

    @Operation(summary = "은행별 계좌 목록 조회", description = "특정 은행에 연동된 계좌 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 은행별 계좌 목록 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMON200",
                                              "message": "성공입니다.",
                                              "result": {
                                                "accountList": [
                                                  {
                                                    "accountName": "KB나라사랑우대통장",
                                                    "balanceAmount": 150000,
                                                    "organization": "0004",
                                                    "createdAt": "2024-05-20T10:00:00"
                                                  }
                                                ],
                                                "totalCount": 1
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<BankResDTO.BankAccountListDTO> getAccountsByBank(@Parameter(description = "은행 코드 (예: 004)") String organization);

    @Operation(summary = "연동 자산 개수 조회", description = "홈화면용 자산(계좌/카드) 개수를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 자산 개수 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "COMMON200",
                                              "message": "성공입니다.",
                                              "result": {
                                                "totalAccountCount": 2,
                                                "totalCardCount": 3,
                                                "totalAssetCount": 5
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<AssetResDTO.AssetSummaryCountDTO> getAssetCount();
}
