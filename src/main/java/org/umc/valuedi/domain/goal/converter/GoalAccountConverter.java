package org.umc.valuedi.domain.goal.converter;

import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.goal.dto.response.GoalAccountResDto;

import java.util.List;

public class GoalAccountConverter {

    public static GoalAccountResDto.UnlinkedBankAccountListDTO toUnlinkedListDTO(List<BankAccount> accounts) {
        List<GoalAccountResDto.UnlinkedBankAccountDTO> items = accounts.stream()
                .map(a -> new GoalAccountResDto.UnlinkedBankAccountDTO(
                        a.getId(),
                        a.getAccountName(),
                        a.getAccountDisplay()
                ))
                .toList();

        return new GoalAccountResDto.UnlinkedBankAccountListDTO(items);
    }
}
