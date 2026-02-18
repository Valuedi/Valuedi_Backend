package org.umc.valuedi.domain.asset.service.command.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetFetchWorker {

    private static final int DEFAULT_SYNC_PERIOD_MONTHS = 3;

    private final CodefAssetService codefAssetService;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final CardRepository cardRepository;
    private final CodefConnectionRepository codefConnectionRepository;
    private final TransactionTemplate transactionTemplate;

    public record FetchResult(
            CodefConnection connection,
            LocalDate startDate,
            List<BankTransaction> bankTransactions,
            List<CardApproval> cardApprovals,
            boolean isSuccess
    ) {}

    @Async("assetFetchExecutor")
    public CompletableFuture<FetchResult> fetchAndConvertData(Long connectionId, Member member) {
        LocalDate today = LocalDate.now();
        LocalDate defaultStartDate = today.minusMonths(DEFAULT_SYNC_PERIOD_MONTHS); // 기본 시작일을 3개월 전으로 설정
        LocalDate overallStartDate = today; // 전체 기관의 시작일 기록용

        // 비동기 스레드에서 새로운 트랜잭션으로 엔티티 조회
        CodefConnection connection = codefConnectionRepository.findByIdWithAccountsAndMember(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        try {
            if (connection.getBusinessType() == BusinessType.BK) {
                List<BankAccount> accounts = connection.getBankAccountList();
                List<BankTransaction> allTransactions = new ArrayList<>();
                
                for (BankAccount account : accounts) {
                    try {
                        // 계좌별로 최적의 startDate 계산 (없으면 3개월 전)
                        LocalDate accountStartDate = bankTransactionRepository.findLatestTransactionDateByAccount(account)
                                .orElse(defaultStartDate);
                        
                        // 계좌별로 API 호출
                        List<BankTransaction> fetched = codefAssetService.getBankTransactions(connection, account, accountStartDate, today);
                        allTransactions.addAll(fetched);

                        // 가장 이른 시작일을 기록
                        if (accountStartDate.isBefore(overallStartDate)) {
                            overallStartDate = accountStartDate;
                        }
                    } catch (Exception e) {
                        log.warn("[ASSET-FETCH-WORKER] 은행 계좌 거래내역 수집 중 오류 발생. 계좌: {}, 기관: {}, 회원 ID: {}",
                                 account.getAccountDisplay(), connection.getOrganization(), member.getId(), e);
                    }
                }
                return CompletableFuture.completedFuture(new FetchResult(connection, overallStartDate, allTransactions, Collections.emptyList(), true));

            } else if (connection.getBusinessType() == BusinessType.CD) {
                // 1. 카드 목록 먼저 동기화 (필수)
                List<Card> fetchedCards = codefAssetService.getCards(connection);
                List<Card> savedCards = Collections.emptyList();
                if (!fetchedCards.isEmpty()) {
                    // 2. 카드 저장 로직만 트랜잭션으로 감싸서 처리
                    savedCards = transactionTemplate.execute(status -> {
                        // 트랜잭션 내부에서 Connection을 다시 조회하여 영속 상태로 만듦
                        CodefConnection managedConnection = codefConnectionRepository.findById(connectionId)
                                .orElseThrow(() -> new IllegalStateException("Connection not found during save"));
                        
                        List<Card> existingCards = cardRepository.findAllByCodefConnection(managedConnection);

                        List<Card> cardsToSave = fetchedCards.stream().map(newCard -> {
                            // 새 카드 객체에 영속 상태의 Connection 할당 (Detached 엔티티 참조 문제 방지)
                            newCard.assignConnection(managedConnection);
                            
                            return existingCards.stream()
                                    .filter(oldCard -> oldCard.getCardNoMasked().equals(newCard.getCardNoMasked()))
                                    .findFirst()
                                    .orElse(newCard);
                        }).toList();

                        return cardRepository.saveAll(cardsToSave);
                    });
                }

                LocalDate cardStartDate = cardApprovalRepository.findLatestApprovalDateByMember(member)
                        .orElse(defaultStartDate);

                List<CardApproval> fetched = codefAssetService.getCardApprovals(connection, savedCards, cardStartDate, today);

                return CompletableFuture.completedFuture(new FetchResult(connection, cardStartDate, Collections.emptyList(), fetched, true));
            }
        } catch (Exception e) {
            log.error("[ASSET-FETCH-WORKER] 자산 데이터 수집 비동기 작업 중 예측하지 못한 오류 발생. 기관: {}, 회원 ID: {}",
                      connection.getOrganization(), member.getId(), e);
            return CompletableFuture.completedFuture(new FetchResult(connection, defaultStartDate, Collections.emptyList(), Collections.emptyList(), false));
        }
        return CompletableFuture.completedFuture(new FetchResult(connection, defaultStartDate, Collections.emptyList(), Collections.emptyList(), true));
    }
}
