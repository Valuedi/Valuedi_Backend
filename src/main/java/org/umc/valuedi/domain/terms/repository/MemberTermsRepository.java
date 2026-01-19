package org.umc.valuedi.domain.terms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.terms.entity.MemberTerms;

import java.util.List;

public interface MemberTermsRepository extends JpaRepository<MemberTerms, Long> {

    // 사용자가 가진 동의 내역 전체 조회
    @Query("""
        SELECT mt
        FROM MemberTerms mt
        JOIN FETCH mt.terms t
        WHERE mt.member.id = :memberId
    """)
    List<MemberTerms> findAllByMemberIdWithTerms(@Param("memberId") Long memberId);

    // 사용자가 동의한 내역만 조회
    @Query("""
        SELECT mt
        FROM MemberTerms mt
        JOIN FETCH mt.terms t
        WHERE mt.member.id = :memberId
          AND mt.isAgreed = true
    """)
    List<MemberTerms> findAgreedByMemberIdWithTerms(@Param("memberId") Long memberId);
}
