package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
public class CodefCardApprovalDTO {

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class Approval {
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
