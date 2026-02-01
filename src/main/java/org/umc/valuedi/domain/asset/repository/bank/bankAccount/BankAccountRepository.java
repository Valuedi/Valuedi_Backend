package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.BankAccount;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /**
     * 전체 활성 계좌 목록 조회 (최신순)
     * Goal과 Left Join Fetch하여 N+1 문제 방지
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "JOIN FETCH ba.codefConnection cc " +
            "LEFT JOIN FETCH Goal g ON g.bankAccount = ba " +
            "WHERE cc.member.id = :memberId " +
            "AND ba.isActive = true " +
            "ORDER BY ba.createdAt DESC")
    List<BankAccount> findAllByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 은행별 활성 계좌 목록 조회
     * Goal과 Left Join Fetch하여 N+1 문제 방지
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "JOIN FETCH ba.codefConnection cc " +
            "LEFT JOIN FETCH Goal g ON g.bankAccount = ba " +
            "WHERE cc.member.id = :memberId " +
            "AND cc.organization = :organization " +
            "AND ba.isActive = true")
    List<BankAccount> findAllByMemberIdAndOrganization(
            @Param("memberId") Long memberId,
            @Param("organization") String organization
    );

    /**
     * 총 활성 계좌 수 카운트
     */
    @Query("SELECT COUNT(ba) FROM BankAccount ba " +
            "WHERE ba.codefConnection.member.id = :memberId " +
            "AND ba.isActive = true")
    long countByMemberId(@Param("memberId") Long memberId);
}
