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
import org.umc.valuedi.global.security.annotation.CurrentMember;

import java.time.LocalDate;
import java.time.YearMonth;
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
    ApiResponse<CardResDTO.CardListDTO> getCards(@CurrentMember Long memberId);

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
    ApiResponse<List<CardResDTO.CardIssuerConnection>> getCardIssuers(@CurrentMember Long memberId);

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
    ApiResponse<CardResDTO.CardListDTO> getCardsByIssuer(
            @Parameter(description = "카드사 코드 (예: 0301)") String issuerCode,
            @CurrentMember Long memberId
    );

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
    ApiResponse<List<BankResDTO.BankConnection>> getBanks(@CurrentMember Long memberId);

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
                                                    "accountId": 1,
                                                    "accountName": "KB나라사랑우대통장",
                                                    "balanceAmount": 150000,
                                                    "organization": "0004",
                                                    "createdAt": "2024-05-20T10:00:00",
                                                    "goalInfo": null
                                                  },
                                                  {
                                                    "accountId": 2,
                                                    "accountName": "KB국민ONE통장",
                                                    "balanceAmount": 300000,
                                                    "organization": "0004",
                                                    "createdAt": "2024-05-21T10:00:00",
                                                    "goalInfo": {
                                                      "goalId": 2,
                                                      "title": "여행"
                                                    }
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
    ApiResponse<BankResDTO.BankAccountListDTO> getAllBankAccounts(@CurrentMember Long memberId);

    @Operation(summary = "은행별 계좌 및 목표 목록 조회", description = "특정 은행에 연동된 계좌와 목표 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 은행별 계좌 목록 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "목표가 있는 경우",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "bankName": "우리은행",
                                                        "totalBalance": 240732,
                                                        "accountList": [
                                                          {
                                                            "accountId": 2,
                                                            "accountName": "저축예금",
                                                            "balanceAmount": 220732,
                                                            "connectedGoalId": null
                                                          },
                                                          {
                                                            "accountId": 3,
                                                            "accountName": "청약저축",
                                                            "balanceAmount": 20000,
                                                            "connectedGoalId": 101
                                                          }
                                                        ],
                                                        "goalList": [
                                                          {
                                                            "goalId": 2,
                                                            "title": "여행행",
                                                            "linkedAccountId": 2
                                                          }
                                                        ]
                                                      }
                                                    }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "목표가 없는 경우",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "COMMON200",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "result": {
                                                        "bankName": "우리은행",
                                                        "totalBalance": 100000,
                                                        "accountList": [
                                                          {
                                                            "accountId": 2,
                                                            "accountName": "저축예금",
                                                            "balanceAmount": 100000,
                                                            "connectedGoalId": null
                                                          }
                                                        ],
                                                        "goalList": []
                                                      }
                                                    }
                                            """
                                    )
                            }
                    )
            )
    })
    ApiResponse<BankResDTO.BankAssetResponse> getAccountsByBank(
            @Parameter(description = "은행 코드 (예: 0020)") String organization,
            @CurrentMember Long memberId
    );

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
    ApiResponse<AssetResDTO.AssetSummaryCountDTO> getAssetCount(@CurrentMember Long memberId);

    @Operation(
            summary = "특정 계좌 거래내역 조회",
            description = """
                    계좌의 거래내역을 최신순으로 페이징 조회합니다.
                    - yearMonth, date 미입력 시 전체 내역을 조회합니다.
                    - transactionType: INCOME(입금), EXPENSE(출금)
                    - afterBalance: 해당 거래 직후의 잔액
                    - currentBalance: 계좌의 현재 잔액 (가장 최근 동기화 기준)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 계좌 거래내역 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "ASSET200_1",
                                              "message": "계좌 거래내역 조회에 성공했습니다.",
                                              "result": {
                                                "totalElements": 25,
                                                "page": 0,
                                                "size": 20,
                                                "totalPages": 2,
                                                "organizationCode": "0020",
                                                "assetName": "저축예금",
                                                "assetNumber": "123-456-789012",
                                                "currentBalance": 1250000,
                                                "content": [
                                                  {
                                                    "transactionAt": "2026-02-13T14:30:00",
                                                    "title": "모바일 OOO 토스뱅크",
                                                    "amount": 50000,
                                                    "transactionType": "INCOME",
                                                    "categoryCode": "TRANSFER",
                                                    "categoryName": "이체",
                                                    "afterBalance": 1250000
                                                  }
                                                ]
                                              }
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ASSET404_1",
                                              "message": "존재하지 않거나 접근 권한이 없는 계좌입니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<AssetResDTO.AssetTransactionResponse> getAccountTransactions(
            @Parameter(description = "계좌 ID", required = true, example = "1") Long accountId,
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", example = "2026-02") YearMonth yearMonth,
            @Parameter(description = "특정 일자 필터 (YYYY-MM-DD)", example = "2026-02-13") LocalDate date,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") int size
    );

    @Operation(
            summary = "특정 카드 승인내역 조회",
            description = """
                    카드의 승인내역을 최신순으로 페이징 조회합니다.
                    - yearMonth, date 미입력 시 전체 내역을 조회합니다.
                    - yearMonth만 입력 시 해당 월 내역을 조회합니다.
                    - date 입력 시 해당 일자 내역만 조회합니다.
                    - transactionType: EXPENSE(일반 결제), INCOME(취소/환불)
                    - currentBalance, afterBalance는 항상 null입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 카드 승인내역 반환",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "ASSET200_2",
                                              "message": "카드 승인내역 조회에 성공했습니다.",
                                              "result": {
                                                "totalElements": 12,
                                                "page": 0,
                                                "size": 20,
                                                "totalPages": 1,
                                                "organizationCode": "0309",
                                                "assetName": "신한카드 Deep Dream",
                                                "assetNumber": "5336-****-****-5678",
                                                "currentBalance": null,
                                                "content": [
                                                  {
                                                    "transactionAt": "2026-02-13T18:20:00",
                                                    "title": "스시초밥",
                                                    "amount": 16500,
                                                    "transactionType": "EXPENSE",
                                                    "categoryCode": "FOOD",
                                                    "categoryName": "식비",
                                                    "afterBalance": null
                                                  }
                                                ]
                                              }
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "카드를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ASSET404_2",
                                              "message": "존재하지 않거나 접근 권한이 없는 카드입니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<AssetResDTO.AssetTransactionResponse> getCardTransactions(
            @Parameter(description = "카드 ID", required = true, example = "1") Long cardId,
            @CurrentMember Long memberId,
            @Parameter(description = "조회 년월 (YYYY-MM)", example = "2026-02") YearMonth yearMonth,
            @Parameter(description = "특정 일자 필터 (YYYY-MM-DD)", example = "2026-02-13") LocalDate date,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") int size
    );
}
