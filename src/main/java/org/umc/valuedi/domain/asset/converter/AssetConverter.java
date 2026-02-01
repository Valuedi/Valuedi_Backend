package org.umc.valuedi.domain.asset.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.connection.enums.Organization;
import org.umc.valuedi.domain.goal.entity.Goal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AssetConverter {

    // 개별 BankAccount 엔티티 -> BankAccountInfo 변환
    public static BankResDTO.BankAccountInfo toBankAccountInfo(BankAccount account) {
        BankResDTO.GoalInfo goalInfo = null;
        if (account.getGoal() != null) {
            Goal goal = account.getGoal();
            
            goalInfo = BankResDTO.GoalInfo.builder()
                    .goalId(goal.getId())
                    .title(goal.getTitle())
                    .build();
        }

        return BankResDTO.BankAccountInfo.builder()
                .accountId(account.getId())
                .accountName(account.getAccountName())
                .balanceAmount(account.getBalanceAmount())
                .organization(account.getCodefConnection().getOrganization()) // 기관코드 추출
                .createdAt(account.getCreatedAt())
                .goalInfo(goalInfo)
                .build();
    }

    // BankAccount 엔티티 리스트 -> BankAccountListDTO 변환
    public static BankResDTO.BankAccountListDTO toBankAccountListDTO(List<BankAccount> bankAccounts) {
        List<BankResDTO.BankAccountInfo> infoList = bankAccounts.stream()
                .map(AssetConverter::toBankAccountInfo)
                .collect(Collectors.toList());

        return BankResDTO.BankAccountListDTO.builder()
                .accountList(infoList)
                .totalCount(infoList.size())
                .build();
    }

    // Card 엔티티 -> CardInfo 변환
    public static CardResDTO.CardInfo toCardInfo(Card card) {
        return CardResDTO.CardInfo.builder()
                .cardName(card.getCardName())
                .cardNoMasked(card.getCardNoMasked())
                .cardType(card.getCardType())
                .organization(card.getCodefConnection().getOrganization())
                .createdAt(card.getCreatedAt())
                .build();
    }

    // Card 엔티티 리스트 -> CardListDTO 변환
    public static CardResDTO.CardListDTO toCardListDTO(List<Card> cards) {
        List<CardResDTO.CardInfo> infoList = cards.stream()
                .map(AssetConverter::toCardInfo)
                .collect(Collectors.toList());

        return CardResDTO.CardListDTO.builder()
                .cardList(infoList)
                .totalCount(infoList.size())
                .build();
    }

    // 자산 개수 요약 DTO 변환
    public static AssetResDTO.AssetSummaryCountDTO toAssetSummaryCountDTO(long accountCount, long cardCount) {
        return AssetResDTO.AssetSummaryCountDTO.builder()
                .totalAccountCount(accountCount)
                .totalCardCount(cardCount)
                .totalAssetCount(accountCount + cardCount)
                .build();
    }

    public static BankResDTO.BankAssetResponse toBankAssetResponse(String organizationCode, List<BankAccount> accounts) {
        String bankName = Organization.getNameByCode(organizationCode);

        // 총 잔액 계산
        long totalBalance = accounts.stream()
                .mapToLong(account -> account.getBalanceAmount() != null ? account.getBalanceAmount() : 0L)
                .sum();

        // 계좌 목록 변환
        List<BankResDTO.AccountInfo> accountList = accounts.stream()
                .map(account -> BankResDTO.AccountInfo.builder()
                        .accountId(account.getId())
                        .accountName(account.getAccountName())
                        .balanceAmount(account.getBalanceAmount())
                        .connectedGoalId(account.getGoal() != null ? account.getGoal().getId() : null)
                        .build())
                .collect(Collectors.toList());

        // 목표 목록 추출
        List<BankResDTO.GoalSimpleInfo> goalList = accounts.stream()
                .map(BankAccount::getGoal)
                .filter(Objects::nonNull)
                .map(goal -> BankResDTO.GoalSimpleInfo.builder()
                        .goalId(goal.getId())
                        .title(goal.getTitle())
                        .linkedAccountId(goal.getBankAccount().getId())
                        .build())
                .collect(Collectors.toList());

        return BankResDTO.BankAssetResponse.builder()
                .bankName(bankName)
                .totalBalance(totalBalance)
                .accountList(accountList)
                .goalList(goalList)
                .build();
    }
}
