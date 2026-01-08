package org.umc.valuedi.global.external.codef.service;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.dto.CodefBankAccountListRequest;
import org.umc.valuedi.global.external.codef.dto.CodefBankTransactionListRequest;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodefBankService {

    private final EasyCodef codef;

    // 보유계좌
    public String getAccountList(CodefBankAccountListRequest req) {
        String url = "/v1/kr/bank/p/account/account-list";

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("connectedId", req.connectedId());
            params.put("organization", req.organization());

            return codef.requestProduct(url, EasyCodefServiceType.DEMO, new HashMap<>(params));
        } catch (Exception e) {
            return "{\"error\":\"보유계좌 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // 거래내역
    public String getTransactionList(CodefBankTransactionListRequest req) {
        String url = "/v1/kr/bank/p/account/transaction-list";

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("connectedId", req.connectedId());
            params.put("organization", req.organization());
            params.put("account", req.account());
            params.put("startDate", req.startDate());
            params.put("endDate", req.endDate());

            return codef.requestProduct(url, EasyCodefServiceType.DEMO, new HashMap<>(params));
        } catch (Exception e) {
            return "{\"error\":\"거래내역 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
