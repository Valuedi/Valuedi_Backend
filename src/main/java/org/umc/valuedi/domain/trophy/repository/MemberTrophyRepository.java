package org.umc.valuedi.domain.trophy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.trophy.entity.MemberTrophy;
import org.umc.valuedi.domain.trophy.entity.Trophy;

import java.util.Optional;

public interface MemberTrophyRepository extends JpaRepository<MemberTrophy, Long> {
    Optional<MemberTrophy> findByMemberIdAndTrophy(Member memberId, Trophy trophy);
}