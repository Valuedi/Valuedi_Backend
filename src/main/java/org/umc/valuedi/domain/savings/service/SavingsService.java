package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.dto.response.SavingsListResponse;
import org.umc.valuedi.infra.fss.exception.FssException;
import org.umc.valuedi.infra.fss.exception.code.FssErrorCode;
import org.umc.valuedi.infra.fss.client.FssSavingsClient;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final FssSavingsClient fssSavingsClient;

    public SavingsListResponse getSavingsList() {

        FssSavingsResponse response = fssSavingsClient.fetchSavingsList(1);

        if (response == null || response.result() == null) {
            throw new FssException(FssErrorCode.EMPTY_RESPONSE);
        }

        String errCd = response.result().errCd();
        if (!"000".equals(errCd)) {
            throw new FssException(FssErrorCode.fromErrCd(errCd));
        }

        return SavingsConverter.toSavingsListResponseDTO(response);
    }
}
