package org.umc.valuedi.domain.mbti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;

import java.util.Optional;

public interface MemberMbtiTestRepository extends JpaRepository<MemberMbtiTest, Long> {

    @Query("""
        select t
        from MemberMbtiTest t
        where t.memberId = :memberId
          and t.isActive = true
          and t.deletedAt is null
        order by t.createdAt desc
    """)
    Optional<MemberMbtiTest> findCurrentActiveTest(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update MemberMbtiTest t
        set t.isActive = false
        where t.memberId = :memberId
          and t.isActive = true
          and t.deletedAt is null
    """)
    int deactivateAllActiveTests(Long memberId);
}
