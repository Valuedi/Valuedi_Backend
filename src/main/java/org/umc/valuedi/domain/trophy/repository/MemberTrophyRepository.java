package org.umc.valuedi.domain.trophy.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.trophy.entity.MemberTrophy;
import org.umc.valuedi.domain.trophy.entity.Trophy;

import java.util.Optional;

public interface MemberTrophyRepository extends JpaRepository<MemberTrophy, Long> {
    @Query("SELECT m FROM MemberTrophy m WHERE m.member = :member AND m.trophy = :trophy")
    Optional<MemberTrophy> findMemberTrophy(@Param("member") Member member, @Param("trophy") Trophy trophy);
}