package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.dto.event.ConnectionSuccessEvent;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.enums.ConnectionStatus;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.external.codef.dto.CodefApiResponse;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;
import org.umc.valuedi.global.external.codef.exception.CodefException;
import org.umc.valuedi.global.external.codef.util.EncryptUtil;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefAccountService {

    private final CodefApiClient codefApiClient;
    private final EncryptUtil encryptUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final CodefConnectionRepository codefConnectionRepository;
    private final MemberRepository memberRepository;

    /**
     * 기존 connectedId 조회
     */
    public String findExistingConnectedId(Long memberId) {
        return codefConnectionRepository.findByMemberId(memberId).stream()
                .filter(c -> c.getStatus() != ConnectionStatus.DELETED)
                .map(CodefConnection::getConnectedId)
                .filter(Objects::nonNull)
                .distinct()
                .findFirst()
                .orElse(null);
    }

    /**
     * CODEF 외부 API 호출 (트랜잭션 밖에서 실행)
     */
    public String callCodefConnectApi(String existingConnectedId, ConnectionReqDTO.Connect request) {
        String encryptedPassword = encryptUtil.encryptRSA(request.getLoginPassword());
        Map<String, Object> requestBody = createRequestBody(request, encryptedPassword);

        if (existingConnectedId == null) {
            return handleFirstCreation(requestBody);
        } else {
            return handleAddition(existingConnectedId, requestBody);
        }
    }

    /**
     * 연동 결과를 DB에 저장
     */
    @Transactional
    public void saveConnection(Long memberId, String connectedId, String organization, BusinessType businessType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        saveConnectionRecord(member, connectedId, organization, businessType);
    }

    /**
     * 금융사 연동 해제 (Codef 계정 삭제)
     */
    public void deleteAccount(String connectedId, String organization, BusinessType businessType) {
        Map<String, Object> accountMap = new HashMap<>();
        accountMap.put("organization", organization);
        accountMap.put("businessType", businessType);
        accountMap.put("countryCode", "KR");
        accountMap.put("clientType", "P");
        accountMap.put("loginType", "1");

        List<Map<String, Object>> accountList = new ArrayList<>();
        accountList.add(accountMap);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectedId", connectedId);
        requestBody.put("accountList", accountList);

        CodefApiResponse<Object> response = codefApiClient.deleteAccount(requestBody);

        if (!response.isSuccess()) {
            throw new CodefException(CodefErrorCode.CODEF_API_DELETE_FAILED);
        }
    }

    /**
     * 최초 등록 처리
     */
    private String handleFirstCreation(Map<String, Object> requestBody) {
        CodefApiResponse<Map<String, Object>> response = codefApiClient.createConnectedId(requestBody);
        if (!response.isSuccess()) {
            log.error("CODEF 계정 생성 실패 - code: {}, message: {}", response.getResult().getCode(), response.getResult().getMessage());
            throw new CodefException(CodefErrorCode.CODEF_API_CREATE_FAILED);
        }
        Map<String, Object> data = response.getData();
        String connectedId = data != null ? (String) data.get("connectedId") : null;

        if (connectedId == null || connectedId.isBlank()) {
            throw new CodefException(CodefErrorCode.CODEF_API_CREATE_FAILED);
        }
        return connectedId;
    }

    /**
     * 기존 ID에 기관 추가 처리
     */
    private String handleAddition(String connectedId, Map<String, Object> requestBody) {
        requestBody.put("connectedId", connectedId); // 기존 ID를 바디에 주입
        CodefApiResponse<Map<String, Object>> response = codefApiClient.addAccountToConnectedId(requestBody);

        if (!response.isSuccess()) {
            log.error("CODEF 계정 추가 실패: {}", response.getResult() != null ? response.getResult().getMessage() : "no result");
            throw new CodefException(CodefErrorCode.CODEF_API_ADD_FAILED);
        }
        // 추가 성공 시에도 기존 아이디를 그대로 반환
        return connectedId;
    }

    /**
     * DB에 연결 기록 저장
     */
    private void saveConnectionRecord(Member member, String connectedId, String organization, BusinessType businessType) {
        boolean isAlreadyLinked = member.getCodefConnectionList().stream()
                .anyMatch(c -> organization.equals(c.getOrganization()));

        if (!isAlreadyLinked) {
            CodefConnection connection = CodefConnection.builder()
                    .organization(organization)
                    .connectedId(connectedId)
                    .businessType(businessType)
                    .member(member)
                    .build();
            member.addCodefConnection(connection);
            log.info("기관 [{}] 연동 정보 저장 완료", organization);

            // 이벤트 발행
            eventPublisher.publishEvent(new ConnectionSuccessEvent(connection));
        }
    }

    /**
     * CODEF 요청 규격(accountList) 생성 헬퍼
     */
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
}
