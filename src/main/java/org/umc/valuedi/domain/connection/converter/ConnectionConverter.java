package org.umc.valuedi.domain.connection.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.dto.res.CardResDTO;
import org.umc.valuedi.domain.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.connection.entity.CodefConnection;
import org.umc.valuedi.domain.connection.enums.Organization;

@Component
public class ConnectionConverter {

    /**
     * CodefConnection -> BankConnectionDTO
     */
    public BankResDTO.BankConnection toBankConnectionDTO(CodefConnection connection) {
        return BankResDTO.BankConnection.builder()
                .id(connection.getId())
                .organizationCode(connection.getOrganization())
                .organizationName(Organization.getNameByCode(connection.getOrganization()))
                .connectedAt(connection.getCreatedAt())
                .status(connection.getStatus())
                .build();
    }

    /**
     * CodefConnection -> ConnectionDTO (공통)
     */
    public ConnectionResDTO.Connection toConnectionDTO(CodefConnection connection) {
        return ConnectionResDTO.Connection.builder()
                .connectionId(connection.getId())
                .organizationCode(connection.getOrganization())
                .organizationName(Organization.getNameByCode(connection.getOrganization()))
                .businessType(connection.getBusinessType())
                .connectedAt(connection.getCreatedAt())
                .status(connection.getStatus())
                .build();
    }

    /**
     * CodefConnection -> CardIssuerConnectionDTO
     */
    public CardResDTO.CardIssuerConnection toCardIssuerConnectionDTO(CodefConnection connection) {
        return CardResDTO.CardIssuerConnection.builder()
                .id(connection.getId())
                .organizationCode(connection.getOrganization())
                .organizationName(Organization.getNameByCode(connection.getOrganization()))
                .connectedAt(connection.getCreatedAt())
                .status(connection.getStatus())
                .build();
    }
}