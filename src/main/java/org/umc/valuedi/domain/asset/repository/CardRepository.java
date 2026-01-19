package org.umc.valuedi.domain.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
    // 사용자의 총 카드 수 카운트
    @Query("SELECT COUNT(c) FROM Card c " +
            "WHERE c.codefConnection.member.id = :memberId " +
            "AND c.isActive = true")
    long countByMemberId(@Param("memberId") Long memberId);
}
