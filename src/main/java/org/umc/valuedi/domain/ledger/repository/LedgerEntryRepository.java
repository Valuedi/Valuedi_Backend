package org.umc.valuedi.domain.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    boolean existsByBankTransactionId(Long bankTransactionId);
    boolean existsByCardApprovalId(Long cardApprovalId);
}
