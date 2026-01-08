package org.umc.valuedi.global.external.codef.dto;

public record CodefBankTransactionListRequest(
        String connectedId,
        String organization,
        String account,
        String startDate,
        String endDate
) {}
