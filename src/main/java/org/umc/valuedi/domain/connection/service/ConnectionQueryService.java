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

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConnectionQueryService {
    private final CodefConnectionRepository connectionRepository;
    private final ConnectionConverter connectionConverter;

    /**
     * 연동된 은행 목록 조회
     */
    public List<BankResDTO.BankConnection> getConnectedBanks(Long memberId) {
        return getConnectionsAndConvert(memberId, BusinessType.BK, connectionConverter::toBankConnectionDTO);
    }

    /**
     * 연동된 카드사 목록 조회
     */
    public List<CardResDTO.CardIssuerConnection> getConnectedCardIssuers(Long memberId) {
        return getConnectionsAndConvert(memberId, BusinessType.CD, connectionConverter::toCardIssuerConnectionDTO);
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

    private <T> List<T> getConnectionsAndConvert(Long memberId, BusinessType businessType, Function<CodefConnection, T> converter) {
        return connectionRepository.findByMemberIdAndBusinessType(memberId, businessType)
                .stream()
                .map(converter)
                .toList();
    }
}
