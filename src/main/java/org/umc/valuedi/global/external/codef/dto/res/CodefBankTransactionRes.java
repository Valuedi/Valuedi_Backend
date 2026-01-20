package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CodefBankTransactionRes {

    @JsonProperty("resTrHistoryList")
    private List<TransactionDetail> resTrHistoryList;

    @Getter
    @NoArgsConstructor
    public static class TransactionDetail {
        @JsonProperty("resAccountTrDate")
        private String resAccountTrDate;
        @JsonProperty("resAccountTrTime")
        private String resAccountTrTime;
        @JsonProperty("resAccountOut")
        private String resAccountOut;
        @JsonProperty("resAccountIn")
        private String resAccountIn;
        @JsonProperty("resAccountDesc3")
        private String resAccountDesc3;
        @JsonProperty("resAfterTranBalance")
        private String resAfterTranBalance;
    }
}
