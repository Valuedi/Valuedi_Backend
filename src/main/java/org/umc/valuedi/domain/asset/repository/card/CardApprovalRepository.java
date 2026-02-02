package org.umc.valuedi.domain.asset.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long>, CardApprovalRepositoryCustom {
    List<CardApproval> findByUsedDateBetween(LocalDate fromDate, LocalDate toDate);
    boolean existsByUsedAmountAndUsedDatetimeBetween(Long amount, LocalDateTime start, LocalDateTime end);

    @Query("SELECT MAX(ca.usedDate) FROM CardApproval ca JOIN ca.card c WHERE c.codefConnection.member = :member")
    Optional<LocalDate> findLatestApprovalDateByMember(@Param("member") Member member);
}
