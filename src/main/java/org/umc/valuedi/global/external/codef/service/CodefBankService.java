package org.umc.valuedi.global.external.codef.service;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodefBankService {

    private final EasyCodef codef;

    /**
     * 보유계좌 조회
     * @param params connectedId, organization 등
     * @return CODEF API 응답 (JSON 문자열)
     */
    public String getAccountList(Map<String, Object> params) {
        String url = "/v1/kr/bank/p/account/account-list";

        try {
            return codef.requestProduct(
                    url,
                    EasyCodefServiceType.DEMO,
                    new HashMap<>(params)
            );
        } catch (Exception e) {
            return "{\"error\":\"보유계좌 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 수시입출 거래내역 조회
     * @param params connectedId, organization, account, startDate, endDate 등
     * @return CODEF API 응답 (JSON 문자열)
     */
    public String getTransactionList(Map<String, Object> params) {
        String url = "/v1/kr/bank/p/account/transaction-list";

        try {
            return codef.requestProduct(
                    url,
                    EasyCodefServiceType.DEMO,
                    new HashMap<>(params)
            );
        } catch (Exception e) {
            return "{\"error\":\"거래내역 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }
}