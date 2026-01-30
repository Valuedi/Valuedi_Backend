package org.umc.valuedi.domain.connection.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Organization {

    // 은행 (00번대)
    WOORI_BANK("0020", "우리은행", BusinessType.BK),
    KB_BANK("0004", "국민은행", BusinessType.BK),
    SHINHAN_BANK("0088", "신한은행", BusinessType.BK),
    HANA_BANK("0081", "하나은행", BusinessType.BK),
    NH_BANK("0011", "농협은행", BusinessType.BK),
    IBK_BANK("0003", "기업은행", BusinessType.BK),

    // 카드 (03번대)
    KB_CARD("0301", "국민카드", BusinessType.CD),
    SHINHAN_CARD("0303", "신한카드", BusinessType.CD),
    WOORI_CARD("0309", "우리카드", BusinessType.CD),
    HANA_CARD("0313", "하나카드", BusinessType.CD),
    NH_CARD("0330", "농협카드", BusinessType.CD),
    ;
    // ... 더 추가 예정

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
