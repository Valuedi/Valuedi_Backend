package org.umc.valuedi.domain.connection.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessType {
    BK("은행"),
    CD("카드");

    private final String description;
}
