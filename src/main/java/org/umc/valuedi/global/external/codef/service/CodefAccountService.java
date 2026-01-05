package org.umc.valuedi.global.external.codef.service;

import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.codef.util.CodefEncryptUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodefAccountService {

    private final EasyCodef codef;
    private final CodefEncryptUtil encryptUtil;

    @SuppressWarnings("unchecked")
    public String createAccount(Map<String, Object> accountData) {
        try {
            // 비밀번호 암호화
            List<Map<String, Object>> accountList =
                    (List<Map<String, Object>>) accountData.get("accountList");

            if (accountList != null && !accountList.isEmpty()) {
                for (Map<String, Object> account : accountList) {
                    if (account.containsKey("password")) {
                        String plainPassword = (String) account.get("password");
                        String encryptedPassword = encryptUtil.encryptPassword(plainPassword);
                        account.put("password", encryptedPassword);
                    }
                }
            }

            return codef.createAccount(
                    EasyCodefServiceType.DEMO,
                    new HashMap<>(accountData)
            );
        } catch (Exception e) {
            return "{\"error\":\"계정 생성 실패\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }
}