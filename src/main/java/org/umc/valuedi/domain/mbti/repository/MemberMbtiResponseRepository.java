package org.umc.valuedi.domain.mbti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiResponse;

public interface MemberMbtiResponseRepository extends JpaRepository<MemberMbtiResponse, Long> {
}
