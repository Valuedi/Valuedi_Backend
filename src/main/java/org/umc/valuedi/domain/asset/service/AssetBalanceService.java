package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetBalanceService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Long syncAndGetLatestBalance(Long memberId, Long accountId) {
        
        BankAccount account = bankAccountRepository.findByIdAndMemberId(accountId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.ACCOUNT_NOT_FOUND));

        return bankTransactionRepository.findTopByBankAccountOrderByTrDatetimeDesc(account)
                .map(BankTransaction::getAfterBalance)
                .orElse(account.getBalanceAmount());
    }
}
