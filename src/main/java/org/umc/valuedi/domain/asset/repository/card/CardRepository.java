package org.umc.valuedi.domain.asset.repository.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    // 사용자의 총 카드 수 카운트
    @Query("SELECT COUNT(c) FROM Card c " +
            "WHERE c.codefConnection.member.id = :memberId " +
            "AND c.isActive = true")
    long countByMemberId(@Param("memberId") Long memberId);

    // 특정 카드사(organization)에 속한 카드 목록 조회
    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.codefConnection cc " +
            "WHERE cc.member.id = :memberId " +
            "AND cc.organization = :organization " +
            "AND c.isActive = true")
    List<Card> findAllByMemberIdAndOrganization(@Param("memberId") Long memberId, @Param("organization") String organization);

    List<Card> findByCodefConnection(CodefConnection codefConnection);
}
