package org.umc.valuedi.domain.connection.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.external.codef.service.CodefAccountService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionCommandService {

    private final CodefAccountService codefAccountService;
    private final CodefConnectionRepository codefConnectionRepository;
    private final ConnectionDeleteCommandService connectionDeleteCommandService;
    private final MemberRepository memberRepository;

    /**
     * 금융사 계정 연동
     */
    @Transactional
    public void connect(Long memberId, ConnectionReqDTO.Connect request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        codefAccountService.connectAccount(member, request);
        log.info("금융사 연동 완료 - memberId: {}, organization: {}", memberId, request.getOrganization());
    }

    /**
     * 금융사 연동 해제
     */
    public void disconnect(Long memberId, Long connectionId) {
        // 조회 및 권한 검증 (트랜잭션 없음)
        CodefConnection connection = codefConnectionRepository.findByIdWithMember(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        if (!connection.getMember().getId().equals(memberId)) {
            throw new ConnectionException(ConnectionErrorCode.CONNECTION_ACCESS_DENIED);
        }

        // 외부 API 호출 (트랜잭션 밖 — 락 보유하지 않음)
        codefAccountService.deleteAccount(
                connection.getConnectedId(),
                connection.getOrganization(),
                connection.getBusinessType()
        );

        // DB 삭제 (짧은 트랜잭션)
        connectionDeleteCommandService.deleteConnectionData(connectionId, connection.getBusinessType());
    }
}
