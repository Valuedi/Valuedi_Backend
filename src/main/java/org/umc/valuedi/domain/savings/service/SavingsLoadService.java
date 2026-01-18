package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.entity.Savings;
import org.umc.valuedi.domain.savings.repository.SavingsRepository;
import org.umc.valuedi.infra.fss.client.FssSavingsClient;
import org.umc.valuedi.infra.fss.dto.response.FssSavingsResponse;
import org.umc.valuedi.infra.fss.exception.FssException;
import org.umc.valuedi.infra.fss.exception.code.FssErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsLoadService {

    private final FssSavingsClient fssSavingsClient;
    private final SavingsRepository savingsRepository;

    @Transactional
    public int loadAndUpsert(Integer pageNo) {
        LocalDateTime loadedAt = LocalDateTime.now();

        FssSavingsResponse response = fssSavingsClient.fetchSavingsList(pageNo);

        if (response == null || response.result() == null) {
            throw new FssException(FssErrorCode.EMPTY_RESPONSE);
        }

        String errCd = response.result().errCd();
        if (!"000".equals(errCd)) {
            throw new FssException(FssErrorCode.fromErrCd(errCd));
        }

        List<Savings> incoming = SavingsConverter.toSavings(response, loadedAt);
        savingsRepository.saveAll(incoming);
        return incoming.size();
    }
}
