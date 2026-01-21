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
import org.umc.valuedi.domain.savings.exception.SavingsException;
import org.umc.valuedi.domain.savings.exception.code.SavingsErrorCode;
import org.umc.valuedi.domain.savings.repository.SavingsRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsService {

    private final SavingsRepository savingsRepository;

    // 적금 목록 조회
    public SavingsResponseDTO.SavingsListResponse getSavingsList(Pageable pageable) {
        // Savings 페이지 조회
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("korCoNm").ascending()
        );
        Page<Savings> page = savingsRepository.findAll(fixed);

        // DTO 변환 후 반환
        return SavingsConverter.toSavingsListResponseDTO(page.getContent(), (int)page.getTotalElements(), page.getNumber() + 1, page.getTotalPages());
    }

    // 적금 상세 조회
    public SavingsResponseDTO.SavingsDetailResponse getSavingsDetail(String finPrdtCd) {
        // Savings 엔티티 조회
        Savings savings = savingsRepository.findByFinPrdtCd(finPrdtCd)
                .orElseThrow(() -> new SavingsException(SavingsErrorCode.SAVINGS_NOT_FOUND));

        // DTO 변환 후 반환
        return SavingsConverter.toSavingsDetailResponseDTO(savings);
    }
}
