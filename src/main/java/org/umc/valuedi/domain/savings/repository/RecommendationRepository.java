package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.savings.entity.Recommendation;
import org.umc.valuedi.domain.savings.entity.Savings;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("""
        select r
        from Recommendation r
        join fetch r.savings s
        left join fetch r.savingsOption so
        where r.member.id = :memberId
          and r.memberMbtiTestId = :memberMbtiTestId
        order by r.score desc, r.createdAt desc
    """)
    List<Recommendation> findLatestByMemberAndMemberMbtiTestId(
            @Param("memberId") Long memberId,
            @Param("memberMbtiTestId") Long memberMbtiTestId,
            Pageable pageable
    );

    @Query("""
    select (count(r) > 0)
    from Recommendation r
    where r.member.id = :memberId
      and r.memberMbtiTestId = :memberMbtiTestId
    """)
    boolean existsByMemberIdAndMemberMbtiTestId(
            @Param("memberId") Long memberId,
            @Param("memberMbtiTestId") Long memberMbtiTestId
    );

    Optional<Savings> findByFinPrdtCd(String finPrdtCd);
}
