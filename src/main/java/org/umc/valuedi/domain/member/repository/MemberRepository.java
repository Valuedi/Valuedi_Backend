package org.umc.valuedi.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
}
