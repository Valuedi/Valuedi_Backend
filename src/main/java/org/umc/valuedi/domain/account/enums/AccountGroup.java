package org.umc.valuedi.domain.account.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountGroup {
    DEPOSIT_TRUST("예금/신탁");
    //FOREIGN_CURRENCY("외화"),
    //FUND("펀드"),
    //LOAN("대출"),
    //INSURANCE("보험");

    private final String description;
}