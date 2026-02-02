package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import org.umc.valuedi.domain.asset.entity.BankAccount;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepositoryCustom {
    // 특정 은행별 활성 계좌 목록 조회
    List<BankAccount> findAllByMemberIdAndOrganization(Long memberId, String organization);

    // 전체 활성 계좌 목록 조회 (최신순)
    List<BankAccount> findAllByMemberId(Long memberId);

    // 총 활성 계좌 수 카운트
    long countByMemberId(Long memberId);

    //목표와 연결되지 않은 계좌 목록 조회
    List<BankAccount> findUnlinkedByMemberId(Long memberId, List<Long> excludeIds);

     //특정 계좌(accountId)가 해당 회원(memberId)의 소유이며 활성 상태인지 조회
    Optional<BankAccount> findByIdAndMemberId(Long accountId, Long memberId);
}
