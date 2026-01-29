package org.umc.valuedi.domain.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    INCOME("입금"),
    EXPENSE("출금"),
    REFUND("취소/환불"); // 추가
    private final String description;
}
