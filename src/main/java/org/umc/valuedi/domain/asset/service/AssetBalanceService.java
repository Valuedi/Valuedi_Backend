package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long syncAndGetLatestBalance(Long memberId, Long accountId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        try {
            AssetResDTO.AssetSyncResult result = assetFetchService.fetchAndSaveLatestData(member);

            // 1. 동기화 결과 DTO에 방금 수집한 실시간 잔액이 있다면 DB 조회 없이 즉시 반환 (레이스 컨디션 방지)
            if (result.hasLatestBalanceFor(accountId)) {
                log.info("[AssetBalanceService] 실시간 동기화 데이터 사용. AccountID: {}, Balance: {}", accountId, result.getLatestBalanceFor(accountId));
                return result.getLatestBalanceFor(accountId);
            }

        } catch (Exception e) {
            log.warn("[AssetBalanceService] 잔액 조회 중 자산 동기화 실패 (기존 DB 잔액 사용): {}", e.getMessage());
        }

        // 2. Fallback: 실시간 데이터가 없거나 동기화 실패 시 DB에서 최신 데이터 조회
        BankAccount account = bankAccountRepository.findByIdAndMemberId(accountId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.ACCOUNT_NOT_FOUND));

        return bankTransactionRepository.findTopByBankAccountOrderByTrDatetimeDesc(account)
                .map(BankTransaction::getAfterBalance)
                .orElse(account.getBalanceAmount());
    }
}