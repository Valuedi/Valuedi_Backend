package org.umc.valuedi.domain.terms.converter;

import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.entity.Terms;

import java.util.List;

public class TermsConverter {

    // entity -> 약관 목록 조회 응답 DTO
    public static TermsResponseDTO.GetTermsList toGetTermsListDTO(List<Terms> termsList) {
        return TermsResponseDTO.GetTermsList.builder()
                .termsLists(termsList.stream()
                        .map(terms -> TermsResponseDTO.TermsList.builder()
                                .termsId(terms.getId())
                                .code(terms.getCode())
                                .title(terms.getTitle())
                                .isRequired(terms.isRequired())
                                .version(terms.getVersion())
                                .build())
                        .toList())
                .build();
    }
}
