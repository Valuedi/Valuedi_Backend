package org.umc.valuedi.global.external.fss.dto;

import java.util.List;

public record FssSavingResponse(Result result) {
    public record Result(
            String err_cd,
            String err_msg,
            String total_count,
            List<BaseInfo> baseList,
            List<OptionInfo> optionList
    ) {}

    public record BaseInfo(
            String fin_prdt_cd,     // 상품 코드
            String kor_co_nm,       // 금융회사 명
            String fin_prdt_nm,     // 금융상품 명
            String join_way,        // 가입 방법
            String mtrt_int,        // 만기 후 이율
            String spcl_cnd         // 우대 조건
    ) {}

    public record OptionInfo(
            String fin_prdt_cd,     // 상품 코드
            String intr_rate_type_nm, // 금리 유형 (단리/복리)
            String save_trm,        // 저축 기간
            Double intr_rate,       // 저축 금리 [기본]
            Double intr_rate2       // 최고 우대 금리
    ) {}
}