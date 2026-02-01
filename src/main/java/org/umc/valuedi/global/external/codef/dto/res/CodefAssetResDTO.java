package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

public class CodefAssetResDTO {

    /**
     * 보유 계좌 목록 조회 응답
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankAccountList {
        @JsonProperty("resDepositTrust")
        private List<BankAccount> resDepositTrust;
        @JsonProperty("resForeignCurrency")
        private List<BankAccount> resForeignCurrency;
        @JsonProperty("resFund")
        private List<BankAccount> resFund;
        @JsonProperty("resLoan")
        private List<BankAccount> resLoan;
        @JsonProperty("resInsurance")
        private List<BankAccount> resInsurance;
    }

    /**
     * 개별 계좌 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankAccount {
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

    /**
     * 계좌 거래 내역 조회 응답
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankTransactionList {
        @JsonProperty("resTrHistoryList")
        private List<BankTransaction> resTrHistoryList;
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

    /**
     * 개별 거래 내역 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankTransaction {
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

    /**
     * 보유 카드 정보
     */
    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Card {
        @JsonProperty("resCardName")
        private String resCardName;
        @JsonProperty("resCardNo")
        private String resCardNo;
        @JsonProperty("resCardType")
        private String resCardType;
        @JsonProperty("resUserNm")
        private String resUserNm;
        @JsonProperty("resSleepYN")
        private String resSleepYN;
        @JsonProperty("resTrafficYN")
        private String resTrafficYN;
        @JsonProperty("resValidPeriod")
        private String resValidPeriod;
        @JsonProperty("resIssueDate")
        private String resIssueDate;
        @JsonProperty("resState")
        private String resState;
        @JsonProperty("resImageLink")
        private String resImageLink;
    }

    /**
     * 카드 승인 내역 정보
     */
    @Getter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardApproval {
        @JsonProperty("resUsedDate")
        private String resUsedDate;
        @JsonProperty("resUsedTime")
        private String resUsedTime;
        @JsonProperty("resCardNo")
        private String resCardNo;
        @JsonProperty("resCardNo1")
        private String resCardNo1;
        @JsonProperty("resMemberStoreName")
        private String resMemberStoreName;
        @JsonProperty("resUsedAmount")
        private String resUsedAmount;
        @JsonProperty("resPaymentType")
        private String resPaymentType;
        @JsonProperty("resInstallmentMonth")
        private String resInstallmentMonth;
        @JsonProperty("resApprovalNo")
        private String resApprovalNo;
        @JsonProperty("resPaymentDueDate")
        private String resPaymentDueDate;
        @JsonProperty("resMemberStoreCorpNo")
        private String resMemberStoreCorpNo;
        @JsonProperty("resMemberStoreAddr")
        private String resMemberStoreAddr;
        @JsonProperty("resMemberStoreType")
        private String resMemberStoreType;
        @JsonProperty("resMemberStoreTelNo")
        private String resMemberStoreTelNo;
        @JsonProperty("resHomeForeignType")
        private String resHomeForeignType;
        @JsonProperty("resAccountCurrency")
        private String resAccountCurrency;
        @JsonProperty("resCancelYN")
        private String resCancelYN;
        @JsonProperty("resCancelAmount")
        private String resCancelAmount;
        @JsonProperty("resVAT")
        private String resVAT;
        @JsonProperty("resCashBack")
        private String resCashBack;
        @JsonProperty("resKRWAmt")
        private String resKRWAmt;
        @JsonProperty("resMemberStoreNo")
        private String resMemberStoreNo;
        @JsonProperty("resCardName")
        private String resCardName;
        @JsonProperty("commStartDate")
        private String commStartDate;
        @JsonProperty("commEndDate")
        private String commEndDate;
    }
}
