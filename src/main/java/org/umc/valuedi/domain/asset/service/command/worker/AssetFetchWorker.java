package org.umc.valuedi.domain.asset.service.command.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
                    try {
                        // 계좌별로 API 호출 및 예외 처리
                        List<BankTransaction> fetched = codefAssetService.getBankTransactions(connection, account, startDate, today);
                        allTransactions.addAll(fetched);
                    } catch (Exception e) {
                        // 특정 계좌에서 오류 발생 시, 로그만 남기고 다음 계좌로 계속 진행
                        log.warn("[ASSET-FETCH-WORKER] 은행 계좌 거래내역 수집 중 오류 발생. 계좌: {}, 기관: {}, 회원 ID: {}", 
                                 account.getAccountDisplay(), connection.getOrganization(), member.getId(), e);
                    }
                }
                // 부분적으로 성공한 데이터라도 성공으로 간주하고 반환
                return CompletableFuture.completedFuture(new FetchResult(connection, startDate, allTransactions, Collections.emptyList(), true));

            } else if (connection.getBusinessType() == BusinessType.CD) {
                // 카드사 API 호출
                List<CardApproval> fetched = codefAssetService.getCardApprovals(connection, startDate, today);
                return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), fetched, true));
            }
        } catch (Exception e) {
            // 전체 connection 단위에서 예측하지 못한 큰 오류 발생 시 실패 처리
            log.error("[ASSET-FETCH-WORKER] 자산 데이터 수집 비동기 작업 중 예측하지 못한 오류 발생. 기관: {}, 회원 ID: {}", 
                      connection.getOrganization(), member.getId(), e);
            return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), Collections.emptyList(), false));
        }
        // 은행, 카드 타입이 아닌 경우
        return CompletableFuture.completedFuture(new FetchResult(connection, startDate, Collections.emptyList(), Collections.emptyList(), true));
    }
}
