package org.umc.valuedi.global.external.codef.service;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodefCardService {

    private final EasyCodef codef;

    /**
     * 보유카드 조회
     * @param params connectedId, organization 등
     * @return CODEF API 응답 (JSON 문자열)
     */
    public String getCardList(Map<String, Object> params) {
        String url = "/v1/kr/card/p/account/card-list";

        try {
            return codef.requestProduct(
                    url,
                    EasyCodefServiceType.DEMO,
                    new HashMap<>(params)
            );
        } catch (Exception e) {
            return "{\"error\":\"보유카드 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 카드 승인내역 조회
     * @param params connectedId, organization, startDate, endDate 등
     * @return CODEF API 응답 (JSON 문자열)
     */
    public String getApprovalList(Map<String, Object> params) {
        String url = "/v1/kr/card/p/account/approval-list";

        try {
            HashMap<String, Object> requestParams = new HashMap<>(params);
            requestParams.putIfAbsent("memberStoreInfoType", "1"); // 1: 가맹점 포함

            return codef.requestProduct(
                    url,
                    EasyCodefServiceType.DEMO,
                    requestParams
            );
        } catch (Exception e) {
            return "{\"error\":\"승인내역 조회 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }
}