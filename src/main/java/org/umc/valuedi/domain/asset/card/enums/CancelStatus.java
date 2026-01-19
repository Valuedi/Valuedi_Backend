package org.umc.valuedi.domain.asset.card.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CancelStatus {
    NORMAL("0", "정상"),
    CANCEL("1", "취소"),
    PARTIAL_CANCEL("2", "부분취소"),
    REJECT("3", "거절");

    private final String code;
    private final String description;
}
