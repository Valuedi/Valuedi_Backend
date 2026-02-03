package org.umc.valuedi.domain.asset.service.command.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.asset.repository.bank.bankTransaction.BankTransactionRepository;
import org.umc.valuedi.domain.asset.repository.card.card.CardRepository;
import org.umc.valuedi.domain.asset.repository.card.cardApproval.CardApprovalRepository;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
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

    private final CodefAssetService codefAssetService;
    private final BankTransactionRepository bankTransactionRepository;
    private final CardApprovalRepository cardApprovalRepository;
    private final CardRepository cardRepository;

    public record FetchResult(
            CodefConnection connection,
            LocalDate startDate,
            List<BankTransaction> bankTransactions,
            List<CardApproval> cardApprovals,
            boolean isSuccess
    ) {}

    @Async("assetFetchExecutor")
    public CompletableFuture<FetchResult> fetchAndConvertData(CodefConnection connection, Member member) {
        LocalDate today = LocalDate.now();
        LocalDate defaultStartDate = today.minusMonths(3); // 기본 시작일을 3개월 전으로 설정
        LocalDate overallStartDate = today; // 전체 기관의 시작일 기록용

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
                if (!fetchedCards.isEmpty()) {
                    cardRepository.saveAll(fetchedCards);
                }

                // 카드사 startDate 계산 (없으면 3개월 전)
                LocalDate cardStartDate = cardApprovalRepository.findLatestApprovalDateByMember(member)
                        .orElse(defaultStartDate);
                
                // 2. 승인 내역 조회 시, 방금 가져온 카드 목록을 직접 전달
                List<CardApproval> fetched = codefAssetService.getCardApprovals(connection, fetchedCards, cardStartDate, today);
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
