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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAssetService {

    private final CodefApiClient codefApiClient;
    private final CodefAssetConverter codefAssetConverter;

    public List<BankAccount> getBankAccounts(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        CodefApiResponse<CodefAssetResDTO.BankAccountList> response = executeApiCall(() -> codefApiClient.getBankAccounts(requestBody));

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

    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account) {
        Map<String, Object> requestBody = createTransactionRequestBody(connection, account);
        CodefApiResponse<CodefAssetResDTO.BankTransactionList> response = executeApiCall(() -> codefApiClient.getBankTransactions(requestBody));

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
        CodefApiResponse<CodefAssetResDTO.CardList> response = executeApiCall(() -> codefApiClient.getCardList(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

        CodefAssetResDTO.CardList cardListResponse = response.getData();
        List<CodefAssetResDTO.Card> allCards = new ArrayList<>();

        if (cardListResponse.getResCardList() != null && !cardListResponse.getResCardList().isEmpty()) {
            // 여러 카드 응답 처리
            allCards.addAll(cardListResponse.getResCardList());
        } else if (cardListResponse.getResCardName() != null) {
            // 단일 카드 응답 처리: CardList DTO의 필드들을 사용하여 Card DTO 객체를 직접 생성
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

    public List<CardApproval> getCardApprovals(CodefConnection connection) {
        Map<String, Object> requestBody = createApprovalRequestBody(connection);
        CodefApiResponse<List<CodefAssetResDTO.CardApproval>> response = executeApiCall(() -> codefApiClient.getCardApprovals(requestBody));

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

    private Map<String, Object> createTransactionRequestBody(CodefConnection connection, BankAccount account) {
        Map<String, Object> body = createAssetRequestBody(connection);
        body.put("account", account.getAccountDisplay());
        LocalDate now = LocalDate.now();
        body.put("startDate", now.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        body.put("endDate", now.format(DateTimeFormatter.BASIC_ISO_DATE));
        body.put("orderBy", "0");
        body.put("inquiryType", "1");
        return body;
    }

    private Map<String, Object> createApprovalRequestBody(CodefConnection connection) {
        Map<String, Object> body = new HashMap<>();
        body.put("connectedId", connection.getConnectedId());
        body.put("organization", connection.getOrganization());
        LocalDate now = LocalDate.now();
        body.put("startDate", now.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        body.put("endDate", now.format(DateTimeFormatter.BASIC_ISO_DATE));
        body.put("orderBy", "0");
        body.put("inquiryType", "1");
        body.put("memberStoreInfoType", "1");
        return body;
    }

    private <T> CodefApiResponse<T> executeApiCall(java.util.function.Supplier<CodefApiResponse<T>> apiCall) {
        try {
            CodefApiResponse<T> response = apiCall.get();
            if (response == null) {
                throw new CodefException(CodefErrorCode.CODEF_RESPONSE_EMPTY);
            }
            return response;
        } catch (Exception e) {
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        }
    }
}
