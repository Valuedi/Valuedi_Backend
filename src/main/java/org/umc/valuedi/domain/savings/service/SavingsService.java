package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.infra.fss.client.FssSavingsClient;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final FssSavingsClient fssSavingsClient;

    public SavingsListResponse getSavingsList() {
        return getSavingsList(1);
    }

    public SavingsListResponse getSavingsList(Integer pageNo) {
        int page = (pageNo == null || pageNo < 1) ? 1 : pageNo;

        FssSavingsResponse response = fssSavingsClient.fetchSavingsList(page);

        return SavingsConverter.toSavingsListResponseDTO(response);
    }
}
