package org.umc.valuedi.domain.goal.converter;

import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.goal.constant.GoalStyleCatalog;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalPrimaryListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GoalConverter {

    private GoalConverter() {}

    public static Goal toEntity(Member member,BankAccount bankAccount, GoalCreateRequestDto req) {
        return Goal.builder()
                .member(member)
                .bankAccount(bankAccount)
                .title(req.title())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .targetAmount(req.targetAmount())
                .status(GoalStatus.ACTIVE)
                .completedAt(null)
                .color(GoalStyleCatalog.normalizeColor(req.colorCode()))
                .icon(req.iconId())
                .build();
    }

    public static void applyPatch(Goal goal, GoalUpdateRequestDto req) {

        if (req.title() != null)
            goal.changeTitle(req.title());

        if (req.startDate() != null)
            goal.changeStartDate(req.startDate());

        if (req.endDate() != null)
            goal.changeEndDate(req.endDate());

        if (req.targetAmount() != null)
            goal.changeTargetAmount(req.targetAmount());

        if (req.colorCode() != null)
            goal.changeColor(req.colorCode());

        if (req.iconId() != null)
            goal.changeIcon(req.iconId());
    }
    public static GoalCreateResponseDto toCreateDto(Goal goal) {
        long remainingDays = calcRemainingDays(goal.getEndDate());

        GoalCreateResponseDto.AccountDto accountDto = null;
        if (goal.getBankAccount() != null) {
            String bankName = null;
            if (goal.getBankAccount().getCodefConnection() != null) {
                bankName = goal.getBankAccount().getCodefConnection().getOrganization();
            }
            String accountNumber = goal.getBankAccount().getAccountDisplay();
            accountDto = new GoalCreateResponseDto.AccountDto(bankName, accountNumber);
        }

        return new GoalCreateResponseDto(
                goal.getId(),
                goal.getTitle(),
                goal.getTargetAmount(),
                goal.getStartDate(),
                goal.getEndDate(),
                remainingDays,
                accountDto,
                goal.getIcon()
        );
    }

    public static GoalListResponseDto.GoalSummaryDto toSummaryDto(
            Goal goal,
            Long savedAmount,
            int achievementRate
    ) {
        Long remainingAmount = Math.max(goal.getTargetAmount() - savedAmount, 0);
        Long remainingDays = calcRemainingDays(goal.getEndDate());

        return new GoalListResponseDto.GoalSummaryDto(
                goal.getId(),
                goal.getTitle(),
                remainingAmount,
                remainingDays,
                achievementRate,
                goal.getStatus(),
                goal.getColor(),
                goal.getIcon()
        );
    }

    public static GoalDetailResponseDto toDetailDto(
            Goal goal,
            Long savedAmount,
            int achievementRate
    ) {
        long remainingDays = calcRemainingDays(goal.getEndDate());

        return new GoalDetailResponseDto(
                goal.getId(),
                goal.getTitle(),
                savedAmount,
                goal.getTargetAmount(),
                remainingDays,
                achievementRate,
                toAccountDto(goal.getBankAccount()),
                goal.getStatus(),
                goal.getColor(),
                goal.getIcon()
        );
    }

    private static GoalDetailResponseDto.AccountDto toAccountDto(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        String bankName = null;
        if (bankAccount.getCodefConnection() != null) {
            bankName = bankAccount.getCodefConnection().getOrganization();
        }

        String accountNumber = bankAccount.getAccountDisplay();

        return new GoalDetailResponseDto.AccountDto(bankName, accountNumber);
    }

    private static long calcRemainingDays(LocalDate endDate) {
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, endDate);
        return Math.max(days, 0);
    }



    public static GoalPrimaryListResponseDto toPrimaryListResponse(List<Goal> goals) {
        return new GoalPrimaryListResponseDto(
                goals.stream()
                        .map(goal -> new GoalPrimaryListResponseDto.GoalPrimarySummaryDto(
                                goal.getId(),              // goalId
                                goal.getTitle(),
                                goal.getTargetAmount(),
                                goal.getIcon()
                        ))
                        .toList()
        );
    }

}
