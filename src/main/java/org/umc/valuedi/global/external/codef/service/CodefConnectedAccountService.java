package org.umc.valuedi.global.external.codef.service;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.dto.CodefConnectedAccountCreateRequest;
import org.umc.valuedi.global.external.codef.util.CodefEncryptUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CodefConnectedAccountService {

    private final EasyCodef codef;
    private final CodefEncryptUtil encryptUtil;

    public String createConnectedAccount(CodefConnectedAccountCreateRequest req) {
        try {
            if (req.accountList() == null || req.accountList().isEmpty()) {
                return "{\"error\":\"accountList가 비어있음\"}";
            }

            Map<String, Object> payload = new HashMap<>();

            List<Map<String, Object>> accountList = new ArrayList<>();
            for (CodefConnectedAccountCreateRequest.Account a : req.accountList()) {
                Map<String, Object> m = new HashMap<>();
                m.put("countryCode", a.countryCode());
                m.put("businessType", a.businessType());
                m.put("clientType", a.clientType());
                m.put("organization", a.organization());
                m.put("loginType", a.loginType());
                m.put("id", a.id());

                String encrypted = encryptUtil.encryptPassword(a.password());
                m.put("password", encrypted);

                accountList.add(m);
            }
            payload.put("accountList", accountList);

            return codef.createAccount(EasyCodefServiceType.DEMO, new HashMap<>(payload));
        } catch (Exception e) {
            return "{\"error\":\"계정 등록 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
