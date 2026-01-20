package org.umc.valuedi.global.external.codef.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.umc.valuedi.global.external.codef.dto.res.*;
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

    /**
     * 보유 계좌 목록 조회
     */
    public List<BankAccount> getBankAccounts(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);

        CodefApiResponse<Object> response = codefApiClient.getBankAccounts(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 보유 계좌 목록 조회 실패: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_BANK_ACCOUNT_LIST_FAILED);
        }

        CodefBankAccountDTO.ListResponse listResponse = objectMapper.convertValue(response.getData(), CodefBankAccountDTO.ListResponse.class);
        List<CodefBankAccountDTO.Account> allAccounts = new ArrayList<>();

        if (listResponse.getResDepositTrust() != null) allAccounts.addAll(listResponse.getResDepositTrust());
        if (listResponse.getResForeignCurrency() != null) allAccounts.addAll(listResponse.getResForeignCurrency());
        if (listResponse.getResFund() != null) allAccounts.addAll(listResponse.getResFund());
        if (listResponse.getResLoan() != null) allAccounts.addAll(listResponse.getResLoan());
        if (listResponse.getResInsurance() != null) allAccounts.addAll(listResponse.getResInsurance());

        return codefAssetConverter.toBankAccountList(allAccounts, connection);
    }

    /**
     * 계좌 거래 내역 조회 (3개월)
     */
    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        requestBody.put("account", account.getAccountDisplay());
        
        LocalDate now = LocalDate.now();
        requestBody.put("startDate", now.minusMonths(3).format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("endDate", now.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("orderBy", "0"); // 최신순
        requestBody.put("inquiryType", "1"); // 전체

        CodefApiResponse<Object> response = codefApiClient.getBankTransactions(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 계좌 거래 내역 조회 실패: {}", response.getResult().getMessage());
            return List.of();
        }

        CodefBankTransactionDTO.Response transactionResponse = objectMapper.convertValue(response.getData(), CodefBankTransactionDTO.Response.class);
        if (transactionResponse.getResTrHistoryList() == null) {
            return List.of();
        }

        return codefAssetConverter.toBankTransactionList(transactionResponse.getResTrHistoryList(), account);
    }

    /**
     * 보유 카드 목록 조회
     */
    public List<Card> getCards(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);

        CodefApiResponse<Object> response = codefApiClient.getCardList(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 보유 카드 목록 조회 실패: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

         Object responseData = response.getData();
        List<CodefCardDTO.Card> cardList = new ArrayList<>();
        
        if (responseData instanceof Map) {
            CodefCardDTO.Card card = objectMapper.convertValue(responseData, CodefCardDTO.Card.class);
            cardList.add(card);
        } else if (responseData instanceof List) {
             List<CodefCardDTO.Card> cards = objectMapper.convertValue(responseData, new TypeReference<List<CodefCardDTO.Card>>() {});
             cardList.addAll(cards);
        } else {
            log.error("CODEF 보유 카드 목록 응답 형식이 예상과 다릅니다. Data: {}", responseData);
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }
        
        return codefAssetConverter.toCardList(cardList, connection);
    }

    /**
     * 카드 승인 내역 조회 (3개월)
     * Postman 성공 요청과 동일한 키만 사용하도록 수정
     */
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

        CodefApiResponse<Object> response = codefApiClient.getCardApprovals(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 카드 승인 내역 조회 실패: {}", response.getResult().getMessage());
            return List.of();
        }

        List<CodefCardApprovalDTO.Approval> approvalList;
        if (response.getData() instanceof List) {
             approvalList = objectMapper.convertValue(response.getData(), new TypeReference<List<CodefCardApprovalDTO.Approval>>() {});
        } else {
             return List.of();
        }

        return codefAssetConverter.toCardApprovalList(approvalList);
    }

    /**
     * CODEF 자산 조회용 공통 요청 바디 생성
     */
    private Map<String, Object> createAssetRequestBody(CodefConnection connection) {
        Map<String, Object> body = new HashMap<>();
        body.put("connectedId", connection.getConnectedId());
        body.put("organization", connection.getOrganization());
        return body;
    }
}
