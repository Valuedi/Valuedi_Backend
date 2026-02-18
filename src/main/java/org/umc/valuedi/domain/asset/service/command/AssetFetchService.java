package org.umc.valuedi.domain.asset.service.command;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.AssetResDTO;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankAccount.BankAccountRepository;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.asset.service.command.worker.AssetFetchWorker;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetFetchService {

    private final EntityManager entityManager;
    private final CodefConnectionRepository codefConnectionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AssetFetchWorker assetFetchWorker;

    private record BankTransactionKey(LocalDateTime trDatetime, Long inAmount, Long outAmount, String desc3) {}
    private record CardApprovalKey(Card card, String approvalNo) {}

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AssetResDTO.AssetSyncResult fetchAndSaveLatestData(Member member) {
        List<CodefConnection> connections = codefConnectionRepository.findByMemberIdWithMember(member.getId());
        LocalDate today = LocalDate.now();

        // 각 기관별로 비동기 API 호출 실행
        List<CompletableFuture<AssetFetchWorker.FetchResult>> futures = connections.stream()
                .map(connection -> assetFetchWorker.fetchAndConvertData(connection, member))
                .toList();

        // 모든 비동기 작업이 완료될 때까지 대기하고 결과 취합
        List<AssetFetchWorker.FetchResult> fetchResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        List<BankTransaction> allFetchedBankTransactions = new ArrayList<>();
        List<CardApproval> allFetchedCardApprovals = new ArrayList<>();

        List<String> successOrganizations = new ArrayList<>();
        List<String> failureOrganizations = new ArrayList<>();
        LocalDate overallMinDate = today;

        for (AssetFetchWorker.FetchResult result : fetchResults) {
            if (result.isSuccess()) {
                successOrganizations.add(result.connection().getOrganization());
                allFetchedBankTransactions.addAll(result.bankTransactions());
                allFetchedCardApprovals.addAll(result.cardApprovals());
                if (result.startDate().isBefore(overallMinDate)) {
                    overallMinDate = result.startDate();
                }
            } else {
                failureOrganizations.add(result.connection().getOrganization());
                log.error("[ASSET-FETCH] 자산 데이터 수집 비동기 작업 실패. 기관: {}, 회원 ID: {}", result.connection().getOrganization(), member.getId());
            }
        }

        Map<Long, Long> realTimeBalances = new HashMap<>();

        // 새로운 데이터 필터링 및 저장
        int totalNewBankTransactions = 0;
        if (!allFetchedBankTransactions.isEmpty()) {
            List<BankTransaction> newBankTransactions = filterNewBankTransactions(allFetchedBankTransactions);
            if (!newBankTransactions.isEmpty()) {
                bankTransactionRepository.bulkInsert(newBankTransactions);
                totalNewBankTransactions = newBankTransactions.size();

                // 계좌 잔액 업데이트 (기존 엔티티 반영)
                updateAccountBalances(newBankTransactions);
            }

            // 수집된 모든 거래내역 중 계좌별 가장 최신 잔액을 추출하여 실시간 데이터 맵에 저장
            allFetchedBankTransactions.stream()
                    .collect(Collectors.groupingBy(tx -> tx.getBankAccount().getId(),
                            Collectors.maxBy(Comparator.comparing(BankTransaction::getTrDatetime))))
                    .forEach((accountId, optTx) -> optTx.ifPresent(tx -> {
                        if (tx.getAfterBalance() != null) {
                            realTimeBalances.put(accountId, tx.getAfterBalance());
                        }
                    }));
        }

        int totalNewCardApprovals = 0;
        if (!allFetchedCardApprovals.isEmpty()) {
            List<CardApproval> newCardApprovals = filterNewCardApprovals(allFetchedCardApprovals);
            if (!newCardApprovals.isEmpty()) {
                cardApprovalRepository.bulkInsert(newCardApprovals);
                totalNewCardApprovals = newCardApprovals.size();
            }
        }

        // JdbcTemplate 사용 후 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();

        return AssetResDTO.AssetSyncResult.builder()
                .newBankTransactionCount(totalNewBankTransactions)
                .newCardApprovalCount(totalNewCardApprovals)
                .successOrganizations(successOrganizations)
                .failureOrganizations(failureOrganizations)
                .fromDate(overallMinDate)
                .toDate(today)
                .latestBalances(realTimeBalances) // 실시간 잔액 데이터 전달
                .build();
    }

    private void updateAccountBalances(List<BankTransaction> transactions) {
        Map<BankAccount, BankTransaction> latestTransactions = transactions.stream()
                .collect(Collectors.groupingBy(BankTransaction::getBankAccount,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(BankTransaction::getTrDatetime)),
                                Optional::get
                        )));

        List<BankAccount> updatedAccounts = new ArrayList<>();

        latestTransactions.forEach((account, latestTransaction) -> {
            if (latestTransaction.getAfterBalance() != null) {
                account.updateBalance(latestTransaction.getAfterBalance());
                updatedAccounts.add(account);
            }
        });

        if (!updatedAccounts.isEmpty()) {
            bankAccountRepository.saveAll(updatedAccounts);
        }
    }

    private List<BankTransaction> filterNewBankTransactions(List<BankTransaction> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        LocalDate minDate = allFetched.stream().map(BankTransaction::getTrDate).min(LocalDate::compareTo).orElse(LocalDate.now());
        List<BankAccount> accounts = allFetched.stream().map(BankTransaction::getBankAccount).distinct().toList();

        List<BankTransaction> existingTransactions = bankTransactionRepository.findByBankAccountInAndTrDatetimeAfter(accounts, minDate.atStartOfDay());

        Set<BankTransactionKey> existingKeys = existingTransactions.stream()
                .map(tx -> new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), "")))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(tx -> !existingKeys.contains(new BankTransactionKey(tx.getTrDatetime(), tx.getInAmount(), tx.getOutAmount(), Objects.toString(tx.getDesc3(), ""))))
                .toList();
    }

    private List<CardApproval> filterNewCardApprovals(List<CardApproval> allFetched) {
        if (allFetched.isEmpty()) return List.of();

        List<Card> cards = allFetched.stream().map(CardApproval::getCard).distinct().toList();
        List<String> approvalNos = allFetched.stream().map(CardApproval::getApprovalNo).distinct().toList();

        List<CardApproval> existingApprovals = cardApprovalRepository.findByCardInAndApprovalNoIn(cards, approvalNos);

        Set<CardApprovalKey> existingKeys = existingApprovals.stream()
                .map(ca -> new CardApprovalKey(ca.getCard(), ca.getApprovalNo()))
                .collect(Collectors.toSet());

        return allFetched.stream()
                .filter(ca -> !existingKeys.contains(new CardApprovalKey(ca.getCard(), ca.getApprovalNo())))
                .toList();
    }
}
