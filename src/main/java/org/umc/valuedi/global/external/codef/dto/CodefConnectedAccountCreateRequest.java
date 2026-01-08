package org.umc.valuedi.global.external.codef.dto;

import java.util.List;

public record CodefConnectedAccountCreateRequest(
        List<Account> accountList
) {
    public record Account(
            String countryCode,
            String businessType,
            String clientType,
            String organization,
            String loginType,
            String id,
            String password
    ) { }
}
