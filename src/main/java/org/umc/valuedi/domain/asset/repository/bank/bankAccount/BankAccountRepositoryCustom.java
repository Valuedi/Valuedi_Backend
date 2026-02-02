package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import org.umc.valuedi.domain.asset.entity.BankAccount;

import java.util.List;

public interface BankAccountRepositoryCustom {
    // 특정 은행별 활성 계좌 목록 조회
    List<BankAccount> findAllByMemberIdAndOrganization(Long memberId, String organization);

    // 전체 활성 계좌 목록 조회 (최신순)
    List<BankAccount> findAllByMemberId(Long memberId);

    // 총 활성 계좌 수 카운트
    long countByMemberId(Long memberId);
}
