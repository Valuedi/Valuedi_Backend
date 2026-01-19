package org.umc.valuedi.domain.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.BankAccount;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // 계좌 목록 조회 (생성일 최신순)
    @Query("SELECT ba FROM BankAccount ba " +
            "JOIN FETCH ba.codefConnection " +
            "WHERE ba.codefConnection.member.id = :memberId " +
            "ORDER BY ba.createdAt DESC")
    List<BankAccount> findAllByMemberIdWithConnectionOrderByCreatedAtDesc(@Param("memberId") Long memberId);
}
