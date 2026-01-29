package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.repository.bank.BankAccountRepository;
import org.umc.valuedi.domain.goal.converter.GoalAccountConverter;
import org.umc.valuedi.domain.goal.dto.response.GoalAccountResDto;
import org.umc.valuedi.domain.goal.repository.GoalRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoalAccountQueryService {

    private final GoalRepository goalRepository;
    private final BankAccountRepository bankAccountRepository;

    public GoalAccountResDto.UnlinkedBankAccountListDTO getUnlinkedAccounts(Long memberId) {
        List<Long> linkedAccountIds = goalRepository.findLinkedBankAccountIdsByMemberId(memberId);

        List<BankAccount> unlinked = bankAccountRepository.findUnlinkedByMemberId(
                memberId,
                linkedAccountIds,
                linkedAccountIds.isEmpty()
        );

        return GoalAccountConverter.toUnlinkedListDTO(unlinked);
    }
}
