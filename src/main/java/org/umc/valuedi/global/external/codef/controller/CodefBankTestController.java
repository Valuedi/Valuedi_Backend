package org.umc.valuedi.global.external.codef.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.global.external.codef.dto.*;
import org.umc.valuedi.global.external.codef.service.CodefAuthService;
import org.umc.valuedi.global.external.codef.service.CodefBankService;
import org.umc.valuedi.global.external.codef.service.CodefConnectedAccountService;

@RestController
@RequestMapping("/api/test/codef/bank")
@RequiredArgsConstructor
public class CodefBankTestController {

    private final CodefAuthService authService;
    private final CodefConnectedAccountService connectedAccountService;
    private final CodefBankService bankService;

    /**
     * 토큰 발급 확인
     * GET /api/test/codef/bank/token
     */
    @GetMapping("/token")
    public CodefResponseEnvelope token() {
        try {
            String token = authService.getAccessToken();
            return CodefResponseEnvelope.ok("{\"access_token\":\"" + token + "\"}");
        } catch (Exception e) {
            return CodefResponseEnvelope.fail("token 발급 실패: " + e.getMessage(), null);
        }
    }

    /**
     * 1) ConnectedId 생성/계정등록(은행)
     * POST /api/test/codef/bank/account/connect
     */
    @PostMapping("/account/connect")
    public CodefResponseEnvelope connect(@RequestBody CodefConnectedAccountCreateRequest request) {
        String raw = connectedAccountService.createConnectedAccount(request);
        return CodefResponseEnvelope.ok(raw);
    }

    /**
     * 2) 보유계좌 조회
     * POST /api/test/codef/bank/account-list
     */
    @PostMapping("/account-list")
    public CodefResponseEnvelope accountList(@RequestBody CodefBankAccountListRequest request) {
        String raw = bankService.getAccountList(request);
        return CodefResponseEnvelope.ok(raw);
    }

    /**
     * 3) 수시입출 거래내역 조회
     * POST /api/test/codef/bank/transaction-list
     */
    @PostMapping("/transaction-list")
    public CodefResponseEnvelope transactionList(@RequestBody CodefBankTransactionListRequest request) {
        String raw = bankService.getTransactionList(request);
        return CodefResponseEnvelope.ok(raw);
    }
}
