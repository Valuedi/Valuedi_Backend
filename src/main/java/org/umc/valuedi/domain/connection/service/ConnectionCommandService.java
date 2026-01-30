package org.umc.valuedi.domain.connection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.service.AssetSyncService;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.external.codef.service.CodefAccountService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionCommandService {

    private final CodefAccountService codefAccountService;
    private final CodefConnectionRepository codefConnectionRepository;
    private final AssetSyncService assetSyncService;
    private final MemberRepository memberRepository;

    /**
     * 금융사 연동 해제
     */
    public void disconnect(Long memberId, Long connectionId) {
        CodefConnection connection = codefConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        if (!connection.getMember().getId().equals(memberId)) {
            throw new ConnectionException(ConnectionErrorCode.CONNECTION_ACCESS_DENIED);
        }

        codefAccountService.deleteAccount(
                connection.getConnectedId(),
                connection.getOrganization(),
                connection.getBusinessType()
        );

        // connection Soft Delete (Cascade에 의해 하위 계좌/카드도 Soft Delete 됨)
        codefConnectionRepository.delete(connection);

        // TODO: [추후 구현] 은행 계좌와 연결된 목표(Goal) Soft Delete 처리 로직 추가 필요
        // if (connection.getBusinessType() == BusinessType.BANK) { ... }
    }

    /**
     * 특정 금융사 연동 건에 대한 거래내역 증분 동기화
     */
    public void syncConnection(Long memberId, Long connectionId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        CodefConnection connection = codefConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        if (!connection.getMember().getId().equals(memberId)) {
            throw new ConnectionException(ConnectionErrorCode.CONNECTION_ACCESS_DENIED);
        }

        log.info("거래내역 수동 동기화 요청 - MemberId: {}, ConnectionId: {}", memberId, connectionId);
        assetSyncService.syncAssetsIncrementally(connectionId);
    }
}
