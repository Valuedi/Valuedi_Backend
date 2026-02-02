package org.umc.valuedi.domain.asset.service.command.worker;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.global.external.codef.service.CodefAssetService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class AssetFetchWorker {

    private final CodefAssetService codefAssetService;

    // 비동기 작업 결과를 담을 내부 DTO
    public record FetchResult(
            CodefConnection connection,
            LocalDate startDate,
            List<BankTransaction> bankTransactions,
            List<CardApproval> cardApprovals,
            boolean isSuccess
    ) {}

    @Async("assetFetchExecutor") // 위에서 설정한 스레드 풀을 사용
    public CompletableFuture<FetchResult> fetchAndConvertData(CodefConnection connection, Member member, LocalDate startDate) {
        try {
            LocalDate today = LocalDate.now();
            if (connection.getBusinessType() == BusinessType.BK) {
                List<BankAccount> accounts = connection.getBankAccountList();
                List<BankTransaction> allTransactions = new ArrayList<>();
                for (BankAccount account : accounts) {
                    // 실제 API 호출
                    List<BankTransaction> fetched = codefAssetService.getBankTransactions(connection, account, startDate, today);
                    allTransactions.addAll(fetched);
                }
                return CompletableFuture.completedFuture(new FetchResult(connection, startDate, allTransactions, Collections.emptyList(), true));

            } else if (connection.getBusinessType() == BusinessType.CD) {
                // 실제 API 호출
                List<CardApproval> fetched = codefAssetService.getCardApprovals(connection, startDate, today);
                return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), fetched, true));
            }
        } catch (Exception e) {
            // 예외 발생 시 실패 결과를 반환
            return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), Collections.emptyList(), false));
        }
        // 은행, 카드 타입이 아닌 경우
        return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), Collections.emptyList(), true));
    }
}
