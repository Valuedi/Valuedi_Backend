package org.umc.valuedi.domain.ledger.repository;

import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import java.util.List;

public interface LedgerEntryRepositoryCustom {
    void bulkInsert(List<LedgerEntry> entries);
}
