package org.umc.valuedi.domain.connection.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.connection.dto.req.ConnectionReqDTO;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.exception.ConnectionException;
import org.umc.valuedi.domain.connection.exception.code.ConnectionErrorCode;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.member.entity.Member;
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
    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    /**
     * 금융사 계정 연동
     */
    public void connect(Long memberId, ConnectionReqDTO.Connect request) {
        log.info("[ConnectionCommandService] [connect] START - Member ID: {}, Org: {}", memberId, request.getOrganization());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        codefAccountService.connectAccount(member, request);
        log.info("[ConnectionCommandService] [connect] END - Member ID: {}", memberId);
    }

    /**
     * 금융사 연동 해제
     */
    public void disconnect(Long memberId, Long connectionId) {
        log.info("[ConnectionCommandService] [disconnect] START - Member ID: {}, Connection ID: {}", memberId, connectionId);
        CodefConnection connection = codefConnectionRepository.findByIdWithMember(connectionId)
                .orElseThrow(() -> new ConnectionException(ConnectionErrorCode.CONNECTION_NOT_FOUND));

        if (!connection.getMember().getId().equals(memberId)) {
            log.error("[ConnectionCommandService] [disconnect] ERROR - Access Denied. Member ID {} tried to access Connection ID {}", memberId, connectionId);
            throw new ConnectionException(ConnectionErrorCode.CONNECTION_ACCESS_DENIED);
        }

        codefAccountService.deleteAccount(
                connection.getConnectedId(),
                connection.getOrganization(),
                connection.getBusinessType()
        );

        //하위 계좌/카드도 Soft Delete
        codefConnectionRepository.delete(connection);

        if (connection.getBusinessType() == BusinessType.BK) {
            // 은행 계좌와 연결된 목표 Soft Delete 처리 (서브쿼리 사용)
            log.debug("[ConnectionCommandService] [disconnect] Deactivating goals for bank connection");
            goalRepository.softDeleteGoalsByConnectionId(connection.getId());
        }
        log.info("[ConnectionCommandService] [disconnect] END - Member ID: {}", memberId);
    }
}
