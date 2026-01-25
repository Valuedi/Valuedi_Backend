package org.umc.valuedi.domain.savings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.savings.converter.SavingsConverter;
import org.umc.valuedi.domain.savings.dto.response.SavingsResponseDTO;
import org.umc.valuedi.domain.savings.entity.Savings;
import org.umc.valuedi.domain.savings.repository.SavingsRepository;
import org.umc.valuedi.global.external.fss.client.FssSavingsClient;
import org.umc.valuedi.global.external.fss.dto.response.FssSavingsResponse;
import org.umc.valuedi.global.external.fss.exception.FssException;
import org.umc.valuedi.global.external.fss.exception.code.FssErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsAdminService {

    private final FssSavingsClient fssSavingsClient;
    private final SavingsRepository savingsRepository;

    // FSS에서 적금 상품을 가져와 DB에 적재
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

    // 적재된 적금 상품 목록 조회
    public SavingsResponseDTO.SavingsListResponse getSavingsList(Pageable pageable) {
        // Savings 페이지 조회
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("korCoNm").ascending()
        );
        Page<Savings> page = savingsRepository.findAll(fixed);

        // DTO 변환 후 반환
        return SavingsConverter.toSavingsListResponseDTO(page.getContent(), (int)page.getTotalElements(), page.getNumber() + 1, page.getTotalPages());
    }
}
