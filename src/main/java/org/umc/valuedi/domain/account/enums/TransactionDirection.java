package org.umc.valuedi.domain.account.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionDirection {
    IN("입금"),
    OUT("출금");

    private final String description;
}
