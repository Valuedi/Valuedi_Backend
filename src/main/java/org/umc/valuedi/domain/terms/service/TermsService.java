package org.umc.valuedi.domain.terms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.terms.converter.TermsConverter;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.entity.Terms;
import org.umc.valuedi.domain.terms.repository.TermsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;

    // 약관 목록 조회
    public TermsResponseDTO.GetTermsList getTermsList() {
        // Terms 목록 조회
        List<Terms> termsList = termsRepository.findActiveTermsOrdered();

        // DTO 변환 후 반환
        return TermsConverter.toGetTermsListDTO(termsList);
    }
}
