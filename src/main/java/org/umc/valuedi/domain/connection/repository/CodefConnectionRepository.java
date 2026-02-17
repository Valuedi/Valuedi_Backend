package org.umc.valuedi.domain.connection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;

import java.util.List;
import java.util.Optional;

public interface CodefConnectionRepository extends JpaRepository<CodefConnection, Long> {

    @Query("SELECT c FROM CodefConnection c JOIN FETCH c.member WHERE c.id = :id")
    Optional<CodefConnection> findByIdWithMember(@Param("id") Long id);

    @Query("SELECT c FROM CodefConnection c JOIN FETCH c.member LEFT JOIN FETCH c.bankAccountList WHERE c.id = :id")
    Optional<CodefConnection> findByIdWithAccountsAndMember(@Param("id") Long id);

    @Query("SELECT c FROM CodefConnection c JOIN FETCH c.member WHERE c.member.id = :memberId")
    List<CodefConnection> findByMemberIdWithMember(@Param("memberId") Long memberId);

    @Query("SELECT c FROM CodefConnection c JOIN FETCH c.member WHERE c.member.id = :memberId AND c.businessType = :businessType")
    List<CodefConnection> findByMemberIdAndBusinessTypeWithMember(@Param("memberId") Long memberId, @Param("businessType") BusinessType businessType);

    List<CodefConnection> findByMemberId(Long memberId);

    List<CodefConnection> findByMemberIdAndBusinessType(Long memberId, BusinessType businessType);
}
