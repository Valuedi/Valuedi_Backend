package org.umc.valuedi.global.external.codef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.external.codef.client.CodefApiClient;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.external.codef.dto.res.CodefApiResponse;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
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

    /**
     * 금융사 계정 연동 메인 로직
     */
    @Transactional
    public void connectAccount(Long memberId, ConnectionReqDTO.Connect request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 기존에 발급받은 Connected ID가 있는지 리스트에서 확인
        String existingConnectedId = member.getCodefConnectionList().stream()
                .map(CodefConnection::getConnectedId)
                .filter(Objects::nonNull)
                .distinct()
                .findFirst()
                .orElse(null);

        // 비밀번호 암호화
        String encryptedPassword = encryptUtil.encrypt(request.getPassword());
        Map<String, Object> requestBody = createRequestBody(request, encryptedPassword);

        String targetConnectedId;

        if (existingConnectedId == null) {
            // 최초 등록 (Create)
            targetConnectedId = handleFirstCreation(requestBody);
        } else {
            // 계정 추가 (Add)
            targetConnectedId = handleAddition(existingConnectedId, requestBody);
        }
        saveConnectionRecord(member, targetConnectedId, request.getOrganization(), request.getBusinessTypeEnum());
    }

    /**
     * 최초 등록 처리
     */
    private String handleFirstCreation(Map<String, Object> requestBody) {
        CodefApiResponse<Map<String, Object>> response = codefApiClient.createConnectedId(requestBody);
        if (!response.isSuccess()) {
            log.error("CODEF 계정 생성 실패: {}", response.getResult().getMessage());
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
        accountMap.put("id", req.getId());
        accountMap.put("password", encryptedPassword);

        List<Map<String, Object>> accountList = new ArrayList<>();
        accountList.add(accountMap);

        Map<String, Object> body = new HashMap<>();
        body.put("accountList", accountList);
        return body;
    }
}