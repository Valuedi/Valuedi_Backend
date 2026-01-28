package org.umc.valuedi.global.external.codef.service;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.dto.event.ConnectionSuccessEvent;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.global.external.codef.dto.res.CodefConnectedIdResDTO;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.util.CodefEncryptUtil;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAccountService {

    private final CodefApiClient codefApiClient;
    private final CodefEncryptUtil encryptUtil;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void connectAccount(Long memberId, ConnectionReqDTO.Connect request) {
        log.info("금융사 연동 요청 - MemberId: {}, Organization: {}", memberId, request.getOrganization());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String encryptedPassword = encryptPassword(request.getLoginPassword());
        Map<String, Object> requestBody = createRequestBody(request, encryptedPassword);

        String connectedId = processCodefAccount(member, requestBody);

        saveConnectionRecord(member, connectedId, request.getOrganization(), request.getBusinessTypeEnum());
        log.info("금융사 연동 완료 - MemberId: {}, Organization: {}", memberId, request.getOrganization());
    }

    @Transactional
    public void deleteAccount(String connectedId, String organization, BusinessType businessType) {
        log.info("금융사 연동 해제 요청 - ConnectedId: {}, Organization: {}", connectedId, organization);
        Map<String, Object> requestBody = createDeleteRequestBody(connectedId, organization, businessType);
        CodefApiResponse<Void> response = executeApiCall(() -> codefApiClient.deleteAccount(requestBody));

        if (!response.isSuccess()) {
            log.error("CODEF 계정 삭제 실패 - Message: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_DELETE_FAILED);
        }
        log.info("금융사 연동 해제 완료 - ConnectedId: {}, Organization: {}", connectedId, organization);
    }

    private String processCodefAccount(Member member, Map<String, Object> requestBody) {
        String existingConnectedId = findExistingConnectedId(member);

        if (existingConnectedId == null) {
            return handleFirstCreation(requestBody);
        } else {
            return handleAddition(existingConnectedId, requestBody);
        }
    }

    private String handleFirstCreation(Map<String, Object> requestBody) {
        CodefApiResponse<CodefConnectedIdResDTO> response = executeApiCall(() -> codefApiClient.createConnectedId(requestBody));
        if (!response.isSuccess()) {
            log.error("CODEF 계정 생성 실패 - Message: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_CREATE_FAILED);
        }
        CodefConnectedIdResDTO data = response.getData();
        String connectedId = data != null ? data.getConnectedId() : null;

        if (connectedId == null || connectedId.isBlank()) {
            throw new CodefException(CodefErrorCode.CODEF_API_CREATE_FAILED);
        }
        return connectedId;
    }

    private String handleAddition(String connectedId, Map<String, Object> requestBody) {
        requestBody.put("connectedId", connectedId);
        CodefApiResponse<Void> response = executeApiCall(() -> codefApiClient.addAccountToConnectedId(requestBody));

        if (!response.isSuccess()) {
            log.error("CODEF 계정 추가 실패 - Message: {}", response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_ADD_FAILED);
        }
        return connectedId;
    }

    private void saveConnectionRecord(Member member, String connectedId, String organization, BusinessType businessType) {
        boolean isAlreadyLinked = member.getCodefConnectionList().stream()
                .anyMatch(c -> organization.equals(c.getOrganization()));

        if (isAlreadyLinked) {
            throw new CodefException(CodefErrorCode.CODEF_DUPLICATE_ORGANIZATION);
        }

        CodefConnection connection = CodefConnection.builder()
                .organization(organization)
                .connectedId(connectedId)
                .businessType(businessType)
                .member(member)
                .build();
        member.addCodefConnection(connection);
        eventPublisher.publishEvent(new ConnectionSuccessEvent(connection));
    }
    
    private String findExistingConnectedId(Member member) {
        return member.getCodefConnectionList().stream()
                .map(CodefConnection::getConnectedId)
                .filter(Objects::nonNull)
                .distinct()
                .findFirst()
                .orElse(null);
    }

    private String encryptPassword(String password) {
        try {
            return encryptUtil.encrypt(password);
        } catch (Exception e) {
            throw new CodefException(CodefErrorCode.CODEF_ENCRYPTION_ERROR);
        }
    }

    private Map<String, Object> createRequestBody(ConnectionReqDTO.Connect req, String encryptedPassword) {
        Map<String, Object> accountMap = new HashMap<>();
        accountMap.put("organization", req.getOrganization());
        accountMap.put("businessType", req.getBusinessType());
        accountMap.put("countryCode", req.getCountryCode());
        accountMap.put("clientType", req.getClientType());
        accountMap.put("loginType", req.getLoginType());
        accountMap.put("id", req.getLoginId());
        accountMap.put("password", encryptedPassword);

        List<Map<String, Object>> accountList = new ArrayList<>();
        accountList.add(accountMap);

        Map<String, Object> body = new HashMap<>();
        body.put("accountList", accountList);
        return body;
    }

    private Map<String, Object> createDeleteRequestBody(String connectedId, String organization, BusinessType businessType) {
        Map<String, Object> accountMap = new HashMap<>();
        accountMap.put("organization", organization);
        accountMap.put("businessType", businessType.toString());
        accountMap.put("countryCode", "KR");
        accountMap.put("clientType", "P");
        accountMap.put("loginType", "1");

        List<Map<String, Object>> accountList = new ArrayList<>();
        accountList.add(accountMap);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connectedId);
        requestBody.put("accountList", accountList);
        return requestBody;
    }

    private <T> CodefApiResponse<T> executeApiCall(java.util.function.Supplier<CodefApiResponse<T>> apiCall) {
        try {
            CodefApiResponse<T> response = apiCall.get();
            if (response == null) {
                log.warn("CODEF API 응답이 null입니다.");
                throw new CodefException(CodefErrorCode.CODEF_RESPONSE_EMPTY);
            }
            return response;
        } catch (RetryableException e) {
            log.error("CODEF API 호출 중 재시도 가능한 오류 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        } catch (FeignException e) {
            log.error("CODEF API 호출 실패 - Status: {}, Body: {}", e.status(), e.contentUTF8(), e);
            throw new CodefException(CodefErrorCode.CODEF_API_CONNECTION_ERROR);
        } catch (Exception e) {
            log.error("CODEF API 호출 중 알 수 없는 오류 발생", e);
            throw new CodefException(CodefErrorCode.CODEF_API_UNHANDLED_ERROR);
        }
    }
}
