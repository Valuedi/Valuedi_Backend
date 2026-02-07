package org.umc.valuedi.domain.goal.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.service.AssetBalanceService;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.validator.GoalValidator;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoalCommandService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AssetBalanceService assetBalanceService;

    // 목표 생성
    public GoalCreateResponseDto createGoal(Long memberId, GoalCreateRequestDto req) {

        GoalValidator.validateDateRange(req.startDate(), req.endDate());
        GoalValidator.validateStyle(req.colorCode(), req.iconId());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 내 계좌 + 활성 상태 검증
        BankAccount account = bankAccountRepository.findByIdAndMemberId(req.bankAccountId(), memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.ACCOUNT_NOT_FOUND));

        // 이미 다른 목표가 이 계좌를 쓰고 있는지 검증
        if (goalRepository.existsByBankAccount_Id(account.getId())) {
            throw new GoalException(GoalErrorCode.ACCOUNT_ALREADY_LINKED_TO_GOAL);
        }

        // 동기화 후 최신 잔액 가져오기
        Long startAmount = assetBalanceService.syncAndGetLatestBalance(memberId, req.bankAccountId());

        // Goal 엔티티 생성 시 bankAccount 포함
        Goal goal = GoalConverter.toEntity(member, account, req, startAmount);
        Goal saved = goalRepository.save(goal);

        return GoalConverter.toCreateDto(saved);
    }

    // 목표 수정
    public void updateGoal(Long memberId, Long goalId, GoalUpdateRequestDto req) {
        Goal goal = goalRepository.findByIdAndMemberId(goalId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new GoalException(GoalErrorCode.GOAL_NOT_EDITABLE);
        }

        if (req.startDate() != null || req.endDate() != null) {
            GoalValidator.validateDateRange(
                    req.startDate() != null ? req.startDate() : goal.getStartDate(),
                    req.endDate() != null ? req.endDate() : goal.getEndDate()
            );
        }

        GoalValidator.validateStyle(req.colorCode(), req.iconId());

        GoalConverter.applyPatch(goal, req);
    }

    // 목표 삭제
    public void deleteGoal(Long memberId, Long goalId) {
        Goal goal = goalRepository.findByIdAndMemberId(goalId, memberId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        goalRepository.delete(goal);
    }
}
