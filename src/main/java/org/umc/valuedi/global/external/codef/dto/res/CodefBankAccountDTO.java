package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CodefBankAccountDTO {

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        @JsonProperty("resAccount")
        private String resAccount;
        @JsonProperty("resAccountDisplay")
        private String resAccountDisplay;
        @JsonProperty("resAccountBalance")
        private String resAccountBalance;
        @JsonProperty("resAccountDeposit")
        private String resAccountDeposit;
        @JsonProperty("resAccountNickName")
        private String resAccountNickName;
        @JsonProperty("resAccountStartDate")
        private String resAccountStartDate;
        @JsonProperty("resAccountEndDate")
        private String resAccountEndDate;
        @JsonProperty("resAccountName")
        private String resAccountName;
        @JsonProperty("resAccountCurrency")
        private String resAccountCurrency;
        @JsonProperty("resAccountLifetime")
        private String resAccountLifetime;
        @JsonProperty("resLastTranDate")
        private String resLastTranDate;
        @JsonProperty("resOverdraftAcctYN")
        private String resOverdraftAcctYN;
        @JsonProperty("resLoanKind")
        private String resLoanKind;
        @JsonProperty("resLoanBalance")
        private String resLoanBalance;
        @JsonProperty("resLoanStartDate")
        private String resLoanStartDate;
        @JsonProperty("resLoanEndDate")
        private String resLoanEndDate;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListResponse {
        @JsonProperty("resDepositTrust")
        private java.util.List<Account> resDepositTrust;
        @JsonProperty("resForeignCurrency")
        private java.util.List<Account> resForeignCurrency;
        @JsonProperty("resFund")
        private java.util.List<Account> resFund;
        @JsonProperty("resLoan")
        private java.util.List<Account> resLoan;
        @JsonProperty("resInsurance")
        private java.util.List<Account> resInsurance;
    }
}
