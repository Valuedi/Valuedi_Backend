package org.umc.valuedi.global.external.codef.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.global.external.codef.service.CodefAccountService;
import org.umc.valuedi.global.external.codef.service.CodefAuthService;
import org.umc.valuedi.global.external.codef.service.CodefBankService;
import org.umc.valuedi.global.external.codef.service.CodefCardService;

import java.util.Map;

@RestController
@RequestMapping("/api/test/codef")
@RequiredArgsConstructor
public class CodefTestController {

    private final CodefAuthService authService;
    private final CodefAccountService accountService;
    private final CodefCardService cardService;
    private final CodefBankService bankService;

    /**
     * 1. Access Token 발급 테스트
     * GET /api/test/codef/token
     */
    @GetMapping("/token")
    public String testToken() {
        return authService.getAccessToken();
    }

    /**
     * 2. ConnectedId 생성 테스트 (계정 등록)
     * POST /api/test/codef/account/create
     */
    @PostMapping("/account/create")
    public String testCreateAccount(@RequestBody Map<String, Object> request) {
        return accountService.createAccount(request);
    }

    /**
     * 3. 보유카드 조회 테스트
     * POST /api/test/codef/card/list
     * }
     */
    @PostMapping("/card/list")
    public String testCardList(@RequestBody Map<String, Object> request) {
        return cardService.getCardList(request);
    }

    /**
     * 4. 카드 승인내역 조회 테스트
     * POST /api/test/codef/card/approval-list
     */
    @PostMapping("/card/approval-list")
    public String testApprovalList(@RequestBody Map<String, Object> request) {
        return cardService.getApprovalList(request);
    }

    /**
     * 5. 보유계좌 조회 테스트
     * POST /api/test/codef/bank/account-list
     */
    @PostMapping("/bank/account-list")
    public String testBankAccountList(@RequestBody Map<String, Object> request) {
        return bankService.getAccountList(request);
    }

    /**
     * 6. 수시입출 거래내역 조회 테스트
     * POST /api/test/codef/bank/transaction-list
     */
    @PostMapping("/bank/transaction-list")
    public String testBankTransactionList(@RequestBody Map<String, Object> request) {
        return bankService.getTransactionList(request);
    }
}