package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;
import org.umc.valuedi.domain.asset.entity.Card;
import org.umc.valuedi.domain.asset.entity.CardApproval;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.global.external.codef.converter.CodefAssetConverter;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.global.external.codef.dto.res.CodefAssetResDTO;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;
import org.umc.valuedi.global.external.codef.util.EncryptUtil;
import org.umc.valuedi.global.external.codef.util.CodefApiExecutor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAssetService {

    private final CodefApiClient codefApiClient;
    private final CodefAssetConverter codefAssetConverter;
    private final ObjectMapper objectMapper;
    private final EncryptUtil encryptUtil;
    private final CodefApiExecutor codefApiExecutor;

    // 기본 조회 기간 (최초 연동 시): 3개월
    private static final int DEFAULT_SEARCH_MONTHS = 3;
    private static final DateTimeFormatter CODEF_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<BankAccount> getBankAccounts(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        CodefApiResponse<CodefAssetResDTO.BankAccountList> response = codefApiExecutor.execute(() -> codefApiClient.getBankAccounts(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_BANK_ACCOUNT_LIST_FAILED);
        }

        CodefAssetResDTO.BankAccountList listResponse = response.getData();
        List<CodefAssetResDTO.BankAccount> allAccounts = new ArrayList<>();

        if (listResponse.getResDepositTrust() != null) allAccounts.addAll(listResponse.getResDepositTrust());
        if (listResponse.getResForeignCurrency() != null) allAccounts.addAll(listResponse.getResForeignCurrency());
        if (listResponse.getResFund() != null) allAccounts.addAll(listResponse.getResFund());
        if (listResponse.getResLoan() != null) allAccounts.addAll(listResponse.getResLoan());
        if (listResponse.getResInsurance() != null) allAccounts.addAll(listResponse.getResInsurance());

        return codefAssetConverter.toBankAccountList(allAccounts, connection);
    }

    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account, String startDate) {
        String originalAccountNo = encryptUtil.decryptAES(account.getAccountNoEnc());
        Map<String, Object> requestBody = createTransactionRequestBody(connection, originalAccountNo, startDate);

        CodefApiResponse<CodefAssetResDTO.BankTransactionList> response =
                codefApiExecutor.execute(() -> codefApiClient.getBankTransactions(requestBody));

        if (!response.isSuccess()) {
            String msg = response.getResult().getMessage();
            if (msg.contains("일치하는 정보가 없습니다") || msg.contains("존재하지 않습니다") || msg.contains("보유계좌")) {
                log.warn("계좌 거래내역 없음 (정상 처리) - Account: {}, Message: {}", account.getAccountDisplay(), msg);
                return List.of();
            }
            throw new CodefException(CodefErrorCode.CODEF_API_INTERNAL_ERROR);
        }

        CodefAssetResDTO.BankTransactionList transactionResponse = response.getData();
        if (transactionResponse.getResTrHistoryList() == null) {
            return List.of();
        }
        return codefAssetConverter.toBankTransactionList(transactionResponse.getResTrHistoryList(), account);
    }

    public List<Card> getCards(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        CodefApiResponse<CodefAssetResDTO.CardList> response = codefApiExecutor.execute(() -> codefApiClient.getCardList(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

        CodefAssetResDTO.CardList cardListResponse = response.getData();
        List<CodefAssetResDTO.Card> allCards = new ArrayList<>();

        if (cardListResponse.getResCardList() != null && !cardListResponse.getResCardList().isEmpty()) {
            allCards.addAll(cardListResponse.getResCardList());
        } else if (cardListResponse.getResCardName() != null) {
            CodefAssetResDTO.Card singleCard = CodefAssetResDTO.Card.builder()
                    .resCardName(cardListResponse.getResCardName())
                    .resCardNo(cardListResponse.getResCardNo())
                    .resCardType(cardListResponse.getResCardType())
                    .resUserNm(cardListResponse.getResUserNm())
                    .resSleepYN(cardListResponse.getResSleepYN())
                    .resTrafficYN(cardListResponse.getResTrafficYN())
                    .resValidPeriod(cardListResponse.getResValidPeriod())
                    .resIssueDate(cardListResponse.getResIssueDate())
                    .resState(cardListResponse.getResState())
                    .resImageLink(cardListResponse.getResImageLink())
                    .build();
            allCards.add(singleCard);
        }

        return codefAssetConverter.toCardList(allCards, connection);
    }

    public List<CardApproval> getCardApprovals(CodefConnection connection, String startDate) {
        Map<String, Object> requestBody = createApprovalRequestBody(connection, startDate);
        CodefApiResponse<List<CodefAssetResDTO.CardApproval>> response = codefApiExecutor.execute(() -> codefApiClient.getCardApprovals(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_INTERNAL_ERROR);
        }

        List<CodefAssetResDTO.CardApproval> approvalList = response.getData();
        if (approvalList == null) {
            return List.of();
        }
        return codefAssetConverter.toCardApprovalList(approvalList);
    }

    private Map<String, Object> createAssetRequestBody(CodefConnection connection) {
        Map<String, Object> body = new HashMap<>();
        body.put("connectedId", connection.getConnectedId());
        body.put("organization", connection.getOrganization());
        return body;
    }

    private void addDateParameters(Map<String, Object> body, String startDate) {
        String finalStartDate = Optional.ofNullable(startDate)
                .filter(s -> !s.isEmpty())
                .orElse(LocalDate.now().minusMonths(DEFAULT_SEARCH_MONTHS).format(CODEF_DATE_FMT));

        body.put("startDate", finalStartDate);
        body.put("endDate", LocalDate.now().format(CODEF_DATE_FMT));
    }

    private Map<String, Object> createTransactionRequestBody(CodefConnection connection, String originalAccountNo, String startDate) {
        Map<String, Object> body = createAssetRequestBody(connection);
        body.put("account", originalAccountNo);
        addDateParameters(body, startDate);
        body.put("orderBy", "0");
        body.put("inquiryType", "1");
        return body;
    }

    private Map<String, Object> createApprovalRequestBody(CodefConnection connection, String startDate) {
        Map<String, Object> body = createAssetRequestBody(connection);
        addDateParameters(body, startDate);
        body.put("orderBy", "0");
        body.put("inquiryType", "1");
        body.put("memberStoreInfoType", "1");
        return body;
    }
}
