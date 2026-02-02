package org.umc.valuedi.domain.asset.repository.bank.bankTransaction;

import org.umc.valuedi.domain.asset.entity.BankTransaction;
import java.util.List;

public interface BankTransactionRepositoryCustom {
    void bulkInsert(List<BankTransaction> transactions);
}
