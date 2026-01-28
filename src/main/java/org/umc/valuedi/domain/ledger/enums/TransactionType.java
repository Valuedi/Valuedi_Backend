package org.umc.valuedi.domain.ledger.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    INCOME("입금"),
    EXPENSE("출금");

    private final String description;
}
