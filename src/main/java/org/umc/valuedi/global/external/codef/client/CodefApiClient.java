package org.umc.valuedi.global.external.codef.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.umc.valuedi.global.external.codef.config.CodefFeignConfig;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.global.external.codef.dto.res.CodefAssetResDTO;
import org.umc.valuedi.global.external.codef.dto.res.CodefConnectedIdResDTO;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "CodefApiClient",
        url = "${codef.api-url}",
        configuration = CodefFeignConfig.class
)
public interface CodefApiClient {

    /**
     * [계정 관리] 최초 등록 (Connected ID 생성)
     */
    @PostMapping("/v1/account/create")
    CodefApiResponse<CodefConnectedIdResDTO> createConnectedId(@RequestBody Map<String, Object> requestBody);

    /**
     * [계정 관리] 계정 추가 (기존 Connected ID에 금융사 추가)
     */
    @PostMapping("/v1/account/add")
    CodefApiResponse<Void> addAccountToConnectedId(@RequestBody Map<String, Object> requestBody);

    /**
     * [계정 관리] 계정 삭제 (연동 해제)
     */
    @PostMapping("/v1/account/delete")
    CodefApiResponse<Void> deleteAccount(@RequestBody Map<String, Object> requestBody);

    /**
     * [은행] 계좌 목록 조회 (보유계좌 조회)
     */
    @PostMapping("/v1/kr/bank/p/account/account-list")
    CodefApiResponse<CodefAssetResDTO.BankAccountList> getBankAccounts(@RequestBody Map<String, Object> requestBody);

    /**
     * [은행] 수시입출 거래내역 조회
     */
    @PostMapping("/v1/kr/bank/p/account/transaction-list")
    CodefApiResponse<CodefAssetResDTO.BankTransactionList> getBankTransactions(@RequestBody Map<String, Object> requestBody);

    /**
     * [카드] 연결된 카드 목록 가져오기 (보유카드 조회)
     */
    @PostMapping("/v1/kr/card/p/account/card-list")
    CodefApiResponse<CodefAssetResDTO.CardList> getCardList(@RequestBody Map<String, Object> requestBody);

    /**
     * [카드] 카드 승인내역 조회
     */
    @PostMapping("/v1/kr/card/p/account/approval-list")
    CodefApiResponse<List<CodefAssetResDTO.CardApproval>> getCardApprovals(@RequestBody Map<String, Object> requestBody);
}
