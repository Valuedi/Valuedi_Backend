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
import org.umc.valuedi.global.external.codef.util.EncryptUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAssetService {

    private final CodefApiClient codefApiClient;
    private final CodefAssetConverter codefAssetConverter;
    private final ObjectMapper objectMapper;
    private final EncryptUtil encryptUtil;

    public List<BankAccount> getBankAccounts(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);

        CodefApiResponse<Object> response = codefApiClient.getBankAccounts(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 보유 계좌 목록 조회 실패: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_BANK_ACCOUNT_LIST_FAILED);
        }

        CodefAssetResDTO.BankAccountList listResponse = objectMapper.convertValue(response.getData(), CodefAssetResDTO.BankAccountList.class);
        List<CodefAssetResDTO.BankAccount> allAccounts = new ArrayList<>();

        if (listResponse.getResDepositTrust() != null) allAccounts.addAll(listResponse.getResDepositTrust());
        if (listResponse.getResForeignCurrency() != null) allAccounts.addAll(listResponse.getResForeignCurrency());
        if (listResponse.getResFund() != null) allAccounts.addAll(listResponse.getResFund());
        if (listResponse.getResLoan() != null) allAccounts.addAll(listResponse.getResLoan());
        if (listResponse.getResInsurance() != null) allAccounts.addAll(listResponse.getResInsurance());

        return codefAssetConverter.toBankAccountList(allAccounts, connection);
    }

    // 기존 메서드 (3개월 전부터 현재까지)
    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account) {
        LocalDate now = LocalDate.now();
        return getBankTransactions(connection, account, now.minusMonths(3), now);
    }

    // 오버로딩된 메서드 (시작일, 종료일 지정)
    public List<BankTransaction> getBankTransactions(CodefConnection connection, BankAccount account, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);
        
        String originalAccountNo = encryptUtil.decryptAES(account.getAccountNoEnc());
        requestBody.put("account", originalAccountNo);
        
        requestBody.put("startDate", startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("endDate", endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("orderBy", "0");
        requestBody.put("inquiryType", "1");

        CodefApiResponse<Object> response = codefApiClient.getBankTransactions(requestBody);

        if (!response.isSuccess()) {
            String msg = response.getResult().getMessage();

            if (msg.contains("일치하는 정보가 없습니다") || msg.contains("존재하지 않습니다") || msg.contains("보유계좌")) {
                // 경고 레벨로 낮춤 (정상적인 예외 상황)
                log.warn("거래내역 조회 불가 계좌 (건너뜀) - 계좌명: {}, 메시지: {}", account.getAccountName(), msg);
            } else {
                log.error("CODEF 계좌 거래 내역 조회 API 오류 - 계좌: {}, 에러: {}", account.getAccountDisplay(), msg);
            }

            return List.of();
        }

        CodefAssetResDTO.BankTransactionList transactionResponse = objectMapper.convertValue(response.getData(), CodefAssetResDTO.BankTransactionList.class);
        if (transactionResponse.getResTrHistoryList() == null) {
            return List.of();
        }

        return codefAssetConverter.toBankTransactionList(transactionResponse.getResTrHistoryList(), account);
    }

    public List<Card> getCards(CodefConnection connection) {
        Map<String, Object> requestBody = createAssetRequestBody(connection);

        CodefApiResponse<Object> response = codefApiClient.getCardList(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 보유 카드 목록 조회 실패: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }

        Object responseData = response.getData();
        List<CodefAssetResDTO.Card> cardList = new ArrayList<>();
        
        if (responseData instanceof Map) {
            CodefAssetResDTO.Card card = objectMapper.convertValue(responseData, CodefAssetResDTO.Card.class);
            cardList.add(card);
        } else if (responseData instanceof List) {
             List<CodefAssetResDTO.Card> cards = objectMapper.convertValue(responseData, new TypeReference<List<CodefAssetResDTO.Card>>() {});
             cardList.addAll(cards);
        } else {
            log.error("CODEF 보유 카드 목록 응답 형식이 예상과 다릅니다. Data: {}", responseData);
            throw new CodefException(CodefErrorCode.CODEF_API_CARD_LIST_FAILED);
        }
        
        return codefAssetConverter.toCardList(cardList, connection);
    }

    // 기존 메서드 (3개월 전부터 현재까지)
    public List<CardApproval> getCardApprovals(CodefConnection connection) {
        LocalDate now = LocalDate.now();
        return getCardApprovals(connection, now.minusMonths(3), now);
    }

    // 오버로딩된 메서드 (시작일, 종료일 지정)
    public List<CardApproval> getCardApprovals(CodefConnection connection, LocalDate startDate, LocalDate endDate) {
        // 이 메서드는 이제 connection 내부의 cardList를 사용하므로, 외부에서 cardList가 채워져 있어야 함.
        return getCardApprovals(connection, connection.getCardList(), startDate, endDate);
    }
    
    // AssetFetchWorker가 사용할 새로운 오버로딩 메서드
    public List<CardApproval> getCardApprovals(CodefConnection connection, List<Card> cards, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connection.getConnectedId());
        requestBody.put("organization", connection.getOrganization());
        
        requestBody.put("startDate", startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("endDate", endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        requestBody.put("orderBy", "0");
        requestBody.put("inquiryType", "1");
        requestBody.put("memberStoreInfoType", "1"); // 가맹점 상세 정보 조회 옵션

        CodefApiResponse<Object> response = codefApiClient.getCardApprovals(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 카드 승인 내역 조회 실패: {}", response.getResult().getMessage());
            return List.of();
        }

        List<CodefAssetResDTO.CardApproval> approvalList;
        if (response.getData() instanceof List) {
             approvalList = objectMapper.convertValue(response.getData(), new TypeReference<List<CodefAssetResDTO.CardApproval>>() {});
        } else {
             return List.of();
        }

        // 승인 내역을 카드에 매핑하기 위해 명시적으로 전달받은 카드 목록을 사용
        return codefAssetConverter.toCardApprovalList(approvalList, cards);
    }

    private Map<String, Object> createAssetRequestBody(CodefConnection connection) {
        Map<String, Object> body = new HashMap<>();
        body.put("connectedId", connection.getConnectedId());
        body.put("organization", connection.getOrganization());
        return body;
    }
}
