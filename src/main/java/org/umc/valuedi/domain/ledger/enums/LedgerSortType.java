package org.umc.valuedi.domain.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LedgerSortType {
    LATEST("최신순"),
    OLDEST("오래된순"),
    AMOUNT_DESC("금액 높은순"),
    AMOUNT_ASC("금액 낮은순");

    private final String description;
}
