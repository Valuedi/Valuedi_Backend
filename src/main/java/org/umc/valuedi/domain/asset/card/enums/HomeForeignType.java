package org.umc.valuedi.domain.asset.card.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HomeForeignType {
    DOMESTIC("1", "국내"),
    FOREIGN("2", "해외");

    private final String code;
    private final String description;
}