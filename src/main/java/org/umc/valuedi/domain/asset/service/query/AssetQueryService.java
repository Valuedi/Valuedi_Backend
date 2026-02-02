package org.umc.valuedi.domain.asset.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.converter.AssetConverter;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssetQueryService {

    private final BankAccountRepository bankAccountRepository;
    private final CardRepository cardRepository;

    /**
     * 연동된 전체 계좌 목록 조회
     */
    public BankResDTO.BankAccountListDTO getAllBankAccounts(Long memberId) {
        List<BankAccount> bankAccounts =
                bankAccountRepository.findAllByMemberId(memberId);
        return AssetConverter.toBankAccountListDTO(bankAccounts);
    }

    /**
     * 은행별 연동된 계좌 및 목표 목록 조회
     */
    public BankResDTO.BankAssetResponse getBankAccountsByOrganization(Long memberId, String organization) {
        List<BankAccount> accounts = bankAccountRepository.findAllByMemberIdAndOrganization(memberId, organization);
        return AssetConverter.toBankAssetResponse(organization, accounts);
    }

    /**
     * 연동된 전체 카드 목록 조회
     */
    public CardResDTO.CardListDTO getAllCards(Long memberId) {
        List<Card> cards =
                cardRepository.findAllByMemberId(memberId);
        return AssetConverter.toCardListDTO(cards);
    }

    /**
     * 카드사별 연동된 카드 목록 조회
     */
    public CardResDTO.CardListDTO getCardsByIssuer(Long memberId, String issuerCode) {
        List<Card> cards = cardRepository.findAllByMemberIdAndOrganization(memberId, issuerCode);
        return AssetConverter.toCardListDTO(cards);
    }

    /**
     * 연동된 자산 총 개수 조회
     */
    public AssetResDTO.AssetSummaryCountDTO getAssetSummaryCount(Long memberId) {
        long accountCount = bankAccountRepository.countByMemberId(memberId);
        long cardCount = cardRepository.countByMemberId(memberId);

        return AssetConverter.toAssetSummaryCountDTO(accountCount, cardCount);
    }
}
