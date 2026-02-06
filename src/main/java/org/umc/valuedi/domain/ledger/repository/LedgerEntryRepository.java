package org.umc.valuedi.domain.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>, LedgerEntryRepositoryCustom {
    boolean existsByBankTransactionId(Long bankTransactionId);
    boolean existsByCardApprovalId(Long cardApprovalId);

    @Query("SELECT le.cardApproval.id FROM LedgerEntry le WHERE le.cardApproval.id IN :ids")
    Set<Long> findExistingCardApprovalIds(@Param("ids") List<Long> ids);

    @Query("SELECT le.bankTransaction.id FROM LedgerEntry le WHERE le.bankTransaction.id IN :ids")
    Set<Long> findExistingBankTransactionIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM LedgerEntry le WHERE le.member = :member AND le.transactionAt >= :start AND le.transactionAt < :end")
    void deleteByMemberAndTransactionAtBetween(
            @Param("member") Member member,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
