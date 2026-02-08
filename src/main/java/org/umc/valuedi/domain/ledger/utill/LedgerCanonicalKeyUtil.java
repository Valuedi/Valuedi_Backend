package org.umc.valuedi.domain.ledger.utill;

import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.enums.CancelStatus;
import org.umc.valuedi.domain.asset.enums.TransactionDirection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LedgerCanonicalKeyUtil {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * Canonical Key 생성
     * 포맷: yyyyMMddHHmm|DIRECTION|AMOUNT
     * 예: 202402011230|EXPENSE|12500
     */
    public static String generate(LocalDateTime occurredAt, String direction, Long amount) {
        return String.format("%s|%s|%d",
                occurredAt.format(TIME_FMT),
                direction,
                Math.abs(amount)
        );
    }

    public static String from(BankTransaction bt) {
        String direction = (bt.getDirection() == TransactionDirection.IN) ? "INCOME" : "EXPENSE";
        Long amount = (bt.getDirection() == TransactionDirection.IN) ? bt.getInAmount() : bt.getOutAmount();
        return generate(bt.getTrDatetime(), direction, amount);
    }

    public static String from(CardApproval ca) {
        // 카드는 정상 승인이면 지출(EXPENSE), 취소면 수입(INCOME) 처리
        String direction = (ca.getCancelYn() == CancelStatus.NORMAL) ? "EXPENSE" : "INCOME";
        return generate(ca.getUsedDatetime(), direction, ca.getUsedAmount());
    }
}
