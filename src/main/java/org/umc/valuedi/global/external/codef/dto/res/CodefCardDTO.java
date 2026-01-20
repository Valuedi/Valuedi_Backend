package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CodefCardDTO {

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
}
