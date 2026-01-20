package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CodefBankAccountRes {

    @JsonProperty("resDepositTrust")
    private List<AccountDetail> resDepositTrust;

    @Getter
    @NoArgsConstructor
    public static class AccountDetail {
        @JsonProperty("resAccount")
        private String resAccount;
        @JsonProperty("resAccountDisplay")
        private String resAccountDisplay;
        @JsonProperty("resAccountBalance")
        private String resAccountBalance;
        @JsonProperty("resAccountDeposit")
        private String resAccountDeposit;
        @JsonProperty("resAccountName")
        private String resAccountName;
        @JsonProperty("resLastTranDate")
        private String resLastTranDate;
    }
}
