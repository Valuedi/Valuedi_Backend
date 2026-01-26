package org.umc.valuedi.global.external.codef.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public List<BankAccount> getBankAccounts(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        CodefApiResponse<Object> response = executeApiCall(() -> codefApiClient.getBankAccounts(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_BANK_ACCOUNT_LIST_FAILED);
        }

        try {
            CodefAssetResDTO.BankAccountList listResponse = objectMapper.convertValue(response.getData(), CodefAssetResDTO.BankAccountList.class);
            List<CodefAssetResDTO.BankAccount> allAccounts = new ArrayList<>();

            if (listResponse.getResDepositTrust() != null) allAccounts.addAll(listResponse.getResDepositTrust());
            if (listResponse.getResForeignCurrency() != null) allAccounts.addAll(listResponse.getResForeignCurrency());
            if (listResponse.getResFund() != null) allAccounts.addAll(listResponse.getResFund());
            if (listResponse.getResLoan() != null) allAccounts.addAll(listResponse.getResLoan());
            if (listResponse.getResInsurance() != null) allAccounts.addAll(listResponse.getResInsurance());

            return codefAssetConverter.toBankAccountList(allAccounts, connection);
        } catch (IllegalArgumentException e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }

    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        requestBody.put("account", account.getAccountDisplay());
        
        LocalDate now = LocalDate.now();
        requestBody.put("startDate", now.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("endDate", now.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("orderBy", "0");
        requestBody.put("inquiryType", "1");

        CodefApiResponse<Object> response = executeApiCall(() -> codefApiClient.getBankTransactions(requestBody));

        if (!response.isSuccess()) {
            String msg = response.getResult().getMessage();
            if (msg.contains("일치하는 정보가 없습니다") || msg.contains("존재하지 않습니다") || msg.contains("보유계좌")) {
                log.warn("계좌 거래내역 없음 (정상 처리) - Account: {}, Message: {}", account.getAccountDisplay(), msg);
                return List.of();
            }
            throw new CodefException(CodefErrorCode.CODEF_API_INTERNAL_ERROR);
        }

        try {
            CodefAssetResDTO.BankTransactionList transactionResponse = objectMapper.convertValue(response.getData(), CodefAssetResDTO.BankTransactionList.class);
            if (transactionResponse.getResTrHistoryList() == null) {
                return List.of();
            }
            return codefAssetConverter.toBankTransactionList(transactionResponse.getResTrHistoryList(), account);
        } catch (IllegalArgumentException e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }

    public List<Card> getCards(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        CodefApiResponse<Object> response = executeApiCall(() -> codefApiClient.getCardList(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

        try {
            Object responseData = response.getData();
            List<CodefAssetResDTO.Card> cardList = new ArrayList<>();
            
            if (responseData instanceof Map) {
                CodefAssetResDTO.Card card = objectMapper.convertValue(responseData, CodefAssetResDTO.Card.class);
                cardList.add(card);
            } else if (responseData instanceof List) {
                 List<CodefAssetResDTO.Card> cards = objectMapper.convertValue(responseData, new TypeReference<List<CodefAssetResDTO.Card>>() {});
                 cardList.addAll(cards);
            } else {
                throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
            }
            
            return codefAssetConverter.toCardList(cardList, connection);
        } catch (IllegalArgumentException e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }

    public List<CardApproval> getCardApprovals(CodefConnection connection) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connection.getConnectedId());
        requestBody.put("organization", connection.getOrganization());
        
        LocalDate now = LocalDate.now();
        requestBody.put("startDate", now.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("endDate", now.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("orderBy", "0");
        requestBody.put("inquiryType", "1");
        requestBody.put("memberStoreInfoType", "1");

        CodefApiResponse<Object> response = executeApiCall(() -> codefApiClient.getCardApprovals(requestBody));

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_INTERNAL_ERROR);
        }

        try {
            List<CodefAssetResDTO.CardApproval> approvalList;
            if (response.getData() instanceof List) {
                 approvalList = objectMapper.convertValue(response.getData(), new TypeReference<List<CodefAssetResDTO.CardApproval>>() {});
            } else {
                 return List.of();
            }
            return codefAssetConverter.toCardApprovalList(approvalList);
        } catch (IllegalArgumentException e) {
            throw new CodefException(CodefErrorCode.CODEF_JSON_PARSE_ERROR);
        }
    }

    private Map<String, Object> createAssetRequestBody(CodefConnection connection) {
        Map<String, Object> body = new HashMap<>();
        body.put("connectedId", connection.getConnectedId());
        body.put("organization", connection.getOrganization());
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
