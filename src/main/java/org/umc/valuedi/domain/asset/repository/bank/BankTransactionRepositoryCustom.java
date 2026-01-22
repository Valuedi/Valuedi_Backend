package org.umc.valuedi.domain.asset.repository.bank;

import org.umc.valuedi.domain.asset.entity.BankTransaction;
import java.util.List;

public interface BankTransactionRepositoryCustom {
    void bulkInsert(List<BankTransaction> transactions);
}
