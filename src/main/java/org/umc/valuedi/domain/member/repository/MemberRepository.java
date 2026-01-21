package org.umc.valuedi.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query(value = "SELECT EXISTS (SELECT 1 FROM member WHERE username = :username)", nativeQuery = true)
    Number _existsByUsernameIncludeDeleted(@Param("username") String username);

    default boolean existsByUsernameIncludeDeleted(String username) {
        Number result = _existsByUsernameIncludeDeleted(username);
        return result != null && result.intValue() == 1;
    }

    Optional<Member> findByUsername(String username);
}
