package org.umc.valuedi.domain.asset.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.converter.AssetConverter;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.repository.BankAccountRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssetQueryService {

    private final BankAccountRepository bankAccountRepository;

    public BankResDTO.BankAccountListDTO getAllBankAccounts(Long memberId) {
        List<BankAccount> bankAccounts =
                bankAccountRepository.findAllByMemberIdWithConnectionOrderByCreatedAtDesc(memberId);
        return AssetConverter.toBankAccountListDTO(bankAccounts);
    }

}
