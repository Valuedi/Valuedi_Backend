package org.umc.valuedi.domain.asset.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {
    LUMP_SUM("1", "일시불"),
    INSTALLMENT("2", "할부"),
    OTHERS("3", "그외");

    private final String code;
    private final String description;
}