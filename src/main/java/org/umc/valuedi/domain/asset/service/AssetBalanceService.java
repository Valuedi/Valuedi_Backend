package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.service.command.AssetFetchService;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetBalanceService {

    private final AssetFetchService assetFetchService;
    private final MemberRepository memberRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;

    /**
     * 자산 동기화를 수행하고, 특정 계좌의 최신 잔액을 반환합니다.
     * 동기화 실패 시 기존 잔액을 반환합니다.
     */
    @Transactional
    public Long syncAndGetLatestBalance(Long memberId, Long accountId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 자산 동기화 시도
        try {
            assetFetchService.fetchAndSaveLatestData(member);
        } catch (Exception e) {
            log.warn("잔액 조회 중 자산 동기화 실패 (기존 잔액 사용): {}", e.getMessage());
        }

        // 계좌 조회 (영속성 컨텍스트 초기화 가능성 고려하여 재조회 권장)
        BankAccount account = bankAccountRepository.findByIdAndMemberId(accountId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.ACCOUNT_NOT_FOUND));

        // 최신 거래내역 기반 잔액 조회
        return bankTransactionRepository.findTopByBankAccountOrderByTrDatetimeDesc(account)
                .map(BankTransaction::getAfterBalance)
                .orElse(account.getBalanceAmount());
    }
}
