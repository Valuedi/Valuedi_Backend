package org.umc.valuedi.infra.fss.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FssSavingsResponse(
        @JsonProperty("result") SavingResult result
) {
   public record SavingResult(
           @JsonProperty("err_cd") String errCd,
           @JsonProperty("err_msg") String errMsg,
           @JsonProperty("total_count") Integer totalCount,
           @JsonProperty("max_page_no") Integer maxPageNo,
           @JsonProperty("now_page_no") Integer nowPageNo,
           @JsonProperty("baseList") List<SavingBaseItem> baseList,
           @JsonProperty("optionList") List<SavingOptionItem> optionList
   ) {}

    // 상품 기본정보
    public record SavingBaseItem (
            @JsonProperty("dcls_month") String dclsMonth,
            @JsonProperty("fin_co_no") String finCoNo,
            @JsonProperty("kor_co_nm") String korCoNm,
            @JsonProperty("fin_prdt_cd") String finPrdtCd,
            @JsonProperty("fin_prdt_nm") String finPrdtNm,
            @JsonProperty("join_way") String joinWay,
            @JsonProperty("mtrt_int") String mtrtInt,
            @JsonProperty("spcl_cnd") String spclCnd,
            @JsonProperty("join_deny") String joinDeny,
            @JsonProperty("join_member") String joinMember,
            @JsonProperty("etc_note") String etcNote,
            @JsonProperty("max_limit") String maxLimit,
            @JsonProperty("dcls_strt_day") String dclsStrtDay,
            @JsonProperty("dcls_end_day") String dclsEndDay,
            @JsonProperty("fin_co_subm_day") String finCoSubmDay
    ) {}

    // 금리 옵션
    public record SavingOptionItem (
            @JsonProperty("dcls_month") String dclsMonth,
            @JsonProperty("fin_co_no") String finCoNo,
            @JsonProperty("fin_prdt_cd") String finPrdtCd,
            @JsonProperty("intr_rate_type") String intrRateType,
            @JsonProperty("intr_rate_type_nm") String intrRateTypeNm,
            @JsonProperty("rsrv_type") String rsrvType,
            @JsonProperty("rsrv_type_nm") String rsrvTypeNm,
            @JsonProperty("save_trm") String saveTrm,
            @JsonProperty("intr_rate") Double intrRate,
            @JsonProperty("intr_rate2") Double intrRate2
    ) {}
}
