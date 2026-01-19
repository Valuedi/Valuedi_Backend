package org.umc.valuedi.domain.asset.connection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.global.external.codef.service.CodefAccountService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionCommandService {

    private final CodefAccountService codefAccountService;

    /**
     * 금융사 계정 연동
     */
    public void connect(Long memberId, ConnectionReqDTO.Connect request) {
        codefAccountService.connectAccount(memberId, request);
        log.info("금융사 연동 완료 - memberId: {}, organization: {}", memberId, request.getOrganization());
    }

    /**
     * 금융사 연동 해제
     */
    public void disconnect(Long memberId, Long connectionId) {
        // TODO: 추후 구현
        log.info("금융사 연동 해제 - memberId: {}, connectionId: {}", memberId, connectionId);
    }
}
