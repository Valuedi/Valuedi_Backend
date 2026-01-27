package org.umc.valuedi.domain.asset.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.CardApproval;

import java.time.LocalDate;
import java.util.List;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long>, CardApprovalRepositoryCustom {
    List<CardApproval> findByUsedDateBetween(LocalDate fromDate, LocalDate toDate);
}
