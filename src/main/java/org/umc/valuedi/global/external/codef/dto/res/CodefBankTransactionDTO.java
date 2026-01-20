package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CodefBankTransactionDTO {

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaction {
        @JsonProperty("resAccountTrDate")
        private String resAccountTrDate;
        @JsonProperty("resAccountTrTime")
        private String resAccountTrTime;
        @JsonProperty("resAccountOut")
        private String resAccountOut;
        @JsonProperty("resAccountIn")
        private String resAccountIn;
        @JsonProperty("resAccountDesc1")
        private String resAccountDesc1;
        @JsonProperty("resAccountDesc2")
        private String resAccountDesc2;
        @JsonProperty("resAccountDesc3")
        private String resAccountDesc3;
        @JsonProperty("resAccountDesc4")
        private String resAccountDesc4;
        @JsonProperty("resAfterTranBalance")
        private String resAfterTranBalance;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("resTrHistoryList")
        private List<Transaction> resTrHistoryList;
        @JsonProperty("resAccountBalance")
        private String resAccountBalance;
        @JsonProperty("resWithdrawalAmt")
        private String resWithdrawalAmt;
        @JsonProperty("resAccountDisplay")
        private String resAccountDisplay;
        @JsonProperty("resAccount")
        private String resAccount;
        @JsonProperty("resAccountName")
        private String resAccountName;
        @JsonProperty("resAccountNickName")
        private String resAccountNickName;
        @JsonProperty("resAccountHolder")
        private String resAccountHolder;
        @JsonProperty("resAccountStartDate")
        private String resAccountStartDate;
        @JsonProperty("resManagementBranch")
        private String resManagementBranch;
        @JsonProperty("resAccountStatus")
        private String resAccountStatus;
        @JsonProperty("resLastTranDate")
        private String resLastTranDate;
        @JsonProperty("resLoanEndDate")
        private String resLoanEndDate;
        @JsonProperty("resLoanLimitAmt")
        private String resLoanLimitAmt;
        @JsonProperty("resInterestRate")
        private String resInterestRate;
        @JsonProperty("commStartDate")
        private String commStartDate;
        @JsonProperty("commEndDate")
        private String commEndDate;
    }
}
