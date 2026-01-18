package org.umc.valuedi.domain.asset.connection.converter;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.asset.bank.dto.res.BankResDTO;
import org.umc.valuedi.domain.asset.connection.dto.res.ConnectionResDTO;
import org.umc.valuedi.domain.asset.connection.entity.CodefConnection;
import org.umc.valuedi.domain.asset.connection.enums.Organization;

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
                .id(connection.getId())
                .organizationCode(connection.getOrganization())
                .organizationName(Organization.getNameByCode(connection.getOrganization()))
                .businessType(connection.getBusinessType())
                .connectedAt(connection.getCreatedAt())
                .status(connection.getStatus())
                .build();
    }
}