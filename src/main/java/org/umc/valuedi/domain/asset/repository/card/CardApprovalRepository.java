package org.umc.valuedi.domain.asset.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.CardApproval;

import java.time.LocalDate;

public interface CardApprovalRepository extends JpaRepository<CardApproval, Long>, CardApprovalRepositoryCustom {
    @Query("SELECT MAX(ca.usedDate) FROM CardApproval ca WHERE ca.card.id = :cardId")
    LocalDate findMaxUsedDateByCardId(@Param("cardId") Long cardId);
}
