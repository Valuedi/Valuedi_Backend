package org.umc.valuedi.domain.asset.converter;

import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;

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
}
