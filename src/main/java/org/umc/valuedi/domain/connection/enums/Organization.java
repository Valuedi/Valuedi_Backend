package org.umc.valuedi.domain.connection.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Organization {

    // 은행 (00번대)
    KDB_BANK("0002", "산업은행", BusinessType.BK),
    IBK_BANK("0003", "기업은행", BusinessType.BK),
    KB_BANK("0004", "국민은행", BusinessType.BK),
    SUHYUP_BANK("0007", "수협은행", BusinessType.BK),
    NH_BANK("0011", "농협은행", BusinessType.BK),
    WOORI_BANK("0020", "우리은행", BusinessType.BK),
    SC_BANK("0023", "SC은행", BusinessType.BK),
    CITI_BANK("0027", "씨티은행", BusinessType.BK),
    // DAEGU_BANK("0031", "대구은행", BusinessType.BK),
    BUSAN_BANK("0032", "부산은행", BusinessType.BK),
    GWANGJU_BANK("0034", "광주은행", BusinessType.BK),
    JEJU_BANK("0035", "제주은행", BusinessType.BK),
    JEONBUK_BANK("0037", "전북은행", BusinessType.BK),
    KYONGNAM_BANK("0039", "경남은행", BusinessType.BK),
    MG_BANK("0045", "새마을금고", BusinessType.BK),
    CU_BANK("0048", "신협은행", BusinessType.BK),
    POST_BANK("0071", "우체국", BusinessType.BK),
    HANA_BANK("0081", "하나은행", BusinessType.BK),
    SHINHAN_BANK("0088", "신한은행", BusinessType.BK),
    K_BANK("0089", "K뱅크", BusinessType.BK),

    // 카드 (03번대)
    KB_CARD("0301", "국민카드", BusinessType.CD),
    // HYUNDAI_CARD("0302", "현대카드", BusinessType.CD),
    SAMSUNG_CARD("0303", "삼성카드", BusinessType.CD),
    NH_CARD("0304", "농협카드", BusinessType.CD),
    BC_CARD("0305", "BC카드", BusinessType.CD),
    SHINHAN_CARD("0306", "신한카드", BusinessType.CD),
    CITI_CARD("0307", "씨티카드", BusinessType.CD),
    WOORI_CARD("0309", "우리카드", BusinessType.CD),
    LOTTE_CARD("0311", "롯데카드", BusinessType.CD),
    HANA_CARD("0313", "하나카드", BusinessType.CD),
    JEONBUK_CARD("0315", "전북카드", BusinessType.CD),
    GWANGJU_CARD("0316", "광주카드", BusinessType.CD),
    SUHYUP_CARD("0320", "수협카드", BusinessType.CD),
    JEJU_CARD("0321", "제주카드", BusinessType.CD);

    ;


    private final String code;
    private final String name;
    private final BusinessType businessType;

    /**
     * 코드로 Organization 찾기
     */
    public static Organization fromCode(String code) {
        for (Organization org : values()) {
            if (org.code.equals(code)) {
                return org;
            }
        }
        throw new IllegalArgumentException("Unknown organization code: " + code);
    }

    /**
     * 코드로 기관명 반환 (매핑 없으면 코드 그대로)
     */
    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }
}
