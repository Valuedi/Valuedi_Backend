package org.umc.valuedi.domain.asset.converter;

import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.Card;

import java.util.List;
import java.util.stream.Collectors;

public class AssetConverter {

    // 개별 BankAccount 엔티티 -> BankAccountInfo 변환
    public static BankResDTO.BankAccountInfo toBankAccountInfo(BankAccount account) {
        return BankResDTO.BankAccountInfo.builder()
                .accountName(account.getAccountName())
                .balanceAmount(account.getBalanceAmount())
                .organization(account.getCodefConnection().getOrganization()) // 기관코드 추출
                .createdAt(account.getCreatedAt())
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
}
