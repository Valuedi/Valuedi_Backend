package org.umc.valuedi.domain.asset.connection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.connection.entity.CodefConnection;
import org.umc.valuedi.domain.asset.connection.enums.BusinessType;

import java.util.List;
import java.util.Optional;

public interface CodefConnectionRepository extends JpaRepository<CodefConnection, Long> {
    // 회원의 모든 연동
    List<CodefConnection> findByMemberId(Long memberId);

    // 특정 기관 연동 여부
    boolean existsByMemberIdAndOrganization(Long memberId, String organization);

    // connectedId 조회용
    Optional<CodefConnection> findFirstByMemberId(Long memberId);

    // 타입별 조회 (은행/카드사)
    List<CodefConnection> findByMemberIdAndBusinessType(Long memberId, BusinessType businessType);

}
