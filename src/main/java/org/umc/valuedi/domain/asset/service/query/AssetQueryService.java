package org.umc.valuedi.domain.asset.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.converter.AssetConverter;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.exception.AssetException;
import org.umc.valuedi.domain.asset.exception.code.AssetErrorCode;
import org.umc.valuedi.domain.asset.repository.AssetTransactionQueryRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;
import org.umc.valuedi.domain.ledger.entity.Category;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.repository.CategoryRepository;
import org.umc.valuedi.domain.ledger.service.query.CategoryMatchingService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssetQueryService {

    private final BankAccountRepository bankAccountRepository;
    private final CardRepository cardRepository;
    private final AssetTransactionQueryRepository assetTransactionQueryRepository;
    private final CategoryMatchingService categoryMatchingService;
    private final CategoryRepository categoryRepository;

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

    /**
     * 특정 계좌의 거래내역 조회
     */
    public AssetResDTO.AssetTransactionResponse getAccountTransactions(
            Long memberId, Long accountId, YearMonth yearMonth, LocalDate date, int page, int size) {

        BankAccount account = assetTransactionQueryRepository
                .findAccountWithConnection(accountId, memberId)
                .orElseThrow(() -> new AssetException(AssetErrorCode.ACCOUNT_NOT_FOUND));

        YearMonth queryYearMonth = (date != null) ? null : yearMonth;
        Page<BankTransaction> txPage = assetTransactionQueryRepository
                .findBankTransactions(accountId, queryYearMonth, date, PageRequest.of(page, size));

        List<Long> ids = txPage.getContent().stream()
                .map(BankTransaction::getId).toList();
        Map<Long, Category> ledgerCategoryMap = assetTransactionQueryRepository
                .findLedgerEntriesForBankTransactions(ids)
                .stream()
                .filter(le -> le.getBankTransaction() != null)
                .collect(Collectors.toMap(
                        le -> le.getBankTransaction().getId(),
                        LedgerEntry::getCategory,
                        (a, b) -> a
                ));

        Category defaultCategory = getCategoryOrThrow("ETC");
        Category transferCategory = getCategoryOrThrow("TRANSFER");

        List<AssetResDTO.AssetTransactionDetail> content = txPage.getContent().stream()
                .map(bt -> {
                    Category cat = ledgerCategoryMap.get(bt.getId());
                    if (cat == null) {
                        String combinedDesc = Stream.of(bt.getDesc2(), bt.getDesc3(), bt.getDesc4())
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(" "));
                        if (categoryMatchingService.isCardSettlement(combinedDesc)) {
                            cat = transferCategory;
                        } else {
                            cat = categoryMatchingService.mapCategoryByKeyword(combinedDesc, defaultCategory);
                        }
                    }
                    return AssetConverter.toBankTransactionDetail(bt, cat);
                })
                .toList();

        return AssetConverter.toAccountTransactionResponse(account, txPage, content);
    }

    /**
     * 특정 카드의 승인내역 조회
     */
    public AssetResDTO.AssetTransactionResponse getCardTransactions(
            Long memberId, Long cardId, YearMonth yearMonth, LocalDate date, int page, int size) {

        Card cardEntity = assetTransactionQueryRepository
                .findCardWithConnection(cardId, memberId)
                .orElseThrow(() -> new AssetException(AssetErrorCode.CARD_NOT_FOUND));

        YearMonth queryYearMonth = (date != null) ? null : yearMonth;
        Page<CardApproval> approvalPage = assetTransactionQueryRepository
                .findCardApprovals(cardId, queryYearMonth, date, PageRequest.of(page, size));

        List<Long> ids = approvalPage.getContent().stream()
                .map(CardApproval::getId).toList();
        Map<Long, Category> ledgerCategoryMap = assetTransactionQueryRepository
                .findLedgerEntriesForCardApprovals(ids)
                .stream()
                .filter(le -> le.getCardApproval() != null)
                .collect(Collectors.toMap(
                        le -> le.getCardApproval().getId(),
                        LedgerEntry::getCategory,
                        (a, b) -> a
                ));

        Category defaultCategory = getCategoryOrThrow("ETC");

        List<AssetResDTO.AssetTransactionDetail> content = approvalPage.getContent().stream()
                .map(ca -> {
                    Category cat = ledgerCategoryMap.get(ca.getId());
                    if (cat == null) {
                        cat = categoryMatchingService.mapCategoryByKeyword(ca.getMerchantType(), null);
                        if (cat == null) cat = categoryMatchingService.mapCategoryByKeyword(ca.getMerchantName(), defaultCategory);
                        if (cat == null) cat = defaultCategory;
                    }
                    return AssetConverter.toCardTransactionDetail(ca, cat);
                })
                .toList();

        return AssetConverter.toCardTransactionResponse(cardEntity, approvalPage, content);
    }

    private Category getCategoryOrThrow(String code) {
        return categoryRepository.findByCode(code)
                .orElseThrow(() -> new AssetException(AssetErrorCode.ASSET_CATEGORY_NOT_FOUND));
    }
}
