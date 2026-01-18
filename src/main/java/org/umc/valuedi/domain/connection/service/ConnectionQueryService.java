package org.umc.valuedi.domain.connection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.connection.converter.ConnectionConverter;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.BusinessType;
import org.umc.valuedi.domain.connection.repository.CodefConnectionRepository;
import org.umc.valuedi.global.external.codef.service.CodefCardService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConnectionQueryService {
    private final CodefConnectionRepository connectionRepository;
    private final CodefCardService codefCardService;
    private final ConnectionConverter connectionConverter;

    /**
     * 연동된 은행 목록 조회
     */
    public List<BankResDTO.BankConnection> getConnectedBanks(Long memberId) {
        List<CodefConnection> connections =
                connectionRepository.findByMemberIdAndBusinessType(
                        memberId,
                        BusinessType.BK
                );

        return connections.stream()
                .map(connectionConverter::toBankConnectionDTO)
                .toList();
    }

    /**
     * 연동된 카드 목록 조회
     */
    public List<CardResDTO.CardConnection> getConnectedCards(Long memberId) {
        // 카드사 연동 확인
        List<CodefConnection> cardConnections =
                connectionRepository.findByMemberIdAndBusinessType(
                        memberId,
                        BusinessType.CD
                );

        if (cardConnections.isEmpty()) {
            return Collections.emptyList();
        }

        String connectedId = cardConnections.get(0).getConnectedId();

        List<String> cardOrganizations = cardConnections.stream()
                .map(CodefConnection::getOrganization)
                .distinct()
                .toList();

        log.info("카드 목록 조회 시작 - memberId: {}, 연동 카드사 수: {}",
                memberId, cardOrganizations.size());

        return codefCardService.getCardList(connectedId, cardOrganizations);
    }

    /**
     * 모든 연동 목록 조회 (은행 + 카드)
     */
    public List<ConnectionResDTO.Connection> getAllConnections(Long memberId) {
        List<CodefConnection> connections =
                connectionRepository.findByMemberId(memberId);

        return connections.stream()
                .map(connectionConverter::toConnectionDTO)
                .toList();
    }
}
