package org.umc.valuedi.domain.connection.service.query;

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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConnectionQueryService {
    private final CodefConnectionRepository connectionRepository;

    /**
     * 연동된 은행 목록 조회
     */
    public List<BankResDTO.BankConnection> getConnectedBanks(Long memberId) {
        List<CodefConnection> connections =
                connectionRepository.findByMemberIdAndBusinessTypeWithMember(
                        memberId,
                        BusinessType.BK
                );

        return connections.stream()
                .map(ConnectionConverter::toBankConnectionDTO)
                .toList();
    }

    /**
     * 연동된 카드사 목록 조회
     */
    public List<CardResDTO.CardIssuerConnection> getConnectedCardIssuers(Long memberId) {
        List<CodefConnection> connections =
                connectionRepository.findByMemberIdAndBusinessTypeWithMember(
                        memberId,
                        BusinessType.CD
                );

        return connections.stream()
                .map(ConnectionConverter::toCardIssuerConnectionDTO)
                .toList();
    }

    /**
     * 모든 연동 목록 조회 (은행 + 카드)
     */
    public List<ConnectionResDTO.Connection> getAllConnections(Long memberId) {
        List<CodefConnection> connections =
                connectionRepository.findByMemberIdWithMember(memberId);

        return connections.stream()
                .map(ConnectionConverter::toConnectionDTO)
                .toList();
    }
}
