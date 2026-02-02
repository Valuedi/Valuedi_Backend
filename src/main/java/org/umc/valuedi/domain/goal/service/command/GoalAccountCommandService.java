package org.umc.valuedi.domain.goal.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepositoryCustom;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalAccountCommandService {

    private final GoalRepository goalRepository;
    private final BankAccountRepositoryCustom bankAccountRepository;

    public void setLinkedAccount(Long memberId, Long goalId, Long accountId) {

        Goal goal = goalRepository.findByIdAndMemberId(goalId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        BankAccount newAccount = bankAccountRepository.findByIdAndMemberId(accountId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.ACCOUNT_NOT_FOUND));

        // 이미 같은 계좌가 연결돼 있으면 그냥 성공
        if (goal.getBankAccount() != null && goal.getBankAccount().getId().equals(accountId)) {
            return;
        }

        // 다른 Goal이 이 계좌를 쓰고 있으면 실패
        if (goalRepository.existsByBankAccount_Id(accountId)) {
            throw new GoalException(GoalErrorCode.ACCOUNT_ALREADY_LINKED_TO_GOAL);
        }

        try {
            // 교체(기존 연결 덮어쓰기)
            goal.linkBankAccount(newAccount);
        } catch (DataIntegrityViolationException e) {
            throw new GoalException(GoalErrorCode.ACCOUNT_ALREADY_LINKED_TO_GOAL);
        }
    }
}
