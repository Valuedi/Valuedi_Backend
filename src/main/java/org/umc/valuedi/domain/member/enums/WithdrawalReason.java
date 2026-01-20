package org.umc.valuedi.domain.member.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WithdrawalReason {

    NOT_HELPFUL("금융 관리에 도움이 되지 않았어요"),
    DIFFICULT_TO_USE("사용이 어려워요"),
    MISSING_FEATURES("필요한 기능이 없어요"),
    SECURITY_CONCERNS("보안이 걱정돼요"),
    FREQUENT_ERRORS("오류가 자주 발생해요"),
    OTHER("기타");

    private final String description;
}
