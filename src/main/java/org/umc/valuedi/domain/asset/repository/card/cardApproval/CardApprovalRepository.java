package org.umc.valuedi.domain.asset.repository.card.cardApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.CardApproval;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long>, CardApprovalRepositoryCustom {
    List<CardApproval> findByUsedDateBetween(LocalDate fromDate, LocalDate toDate);
    boolean existsByUsedAmountAndUsedDatetimeBetween(Long amount, LocalDateTime start, LocalDateTime end);
}
