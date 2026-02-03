package org.umc.valuedi.domain.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;

import java.util.List;
import java.util.Set;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, LedgerEntryRepositoryCustom {
    boolean existsByBankTransactionId(Long bankTransactionId);
    boolean existsByCardApprovalId(Long cardApprovalId);

    @Query("SELECT le.cardApproval.id FROM LedgerEntry le WHERE le.cardApproval.id IN :ids")
    Set<Long> findExistingCardApprovalIds(@Param("ids") List<Long> ids);

    @Query("SELECT le.bankTransaction.id FROM LedgerEntry le WHERE le.bankTransaction.id IN :ids")
    Set<Long> findExistingBankTransactionIds(@Param("ids") List<Long> ids);
}
