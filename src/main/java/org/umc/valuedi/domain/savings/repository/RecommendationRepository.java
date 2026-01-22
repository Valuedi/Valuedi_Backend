package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.savings.entity.Recommendation;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("""
        select r
        from Recommendation r
        join fetch r.savings s
        left join fetch r.savingsOption so
        where r.member.id = :memberId
        order by r.score desc, r.createdAt desc
    """)
    List<Recommendation> findLatestByMember(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
