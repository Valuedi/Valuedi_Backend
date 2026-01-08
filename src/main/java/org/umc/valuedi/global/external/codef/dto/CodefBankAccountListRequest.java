package org.umc.valuedi.global.external.codef.dto;

public record CodefBankAccountListRequest(
        String connectedId,
        String organization
) {}
