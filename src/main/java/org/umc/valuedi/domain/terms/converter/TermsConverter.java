package org.umc.valuedi.domain.terms.converter;

import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.entity.MemberTerms;
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

    // entity -> 사용자가 동의한 약관 조회
    public static TermsResponseDTO.GetMemberAgreements toGetMemberAgreementsDTO(List<MemberTerms> memberTermsList) {
        return TermsResponseDTO.GetMemberAgreements.builder()
                .agreements(memberTermsList.stream()
                        .map(mt -> TermsResponseDTO.MemberAgreement.builder()
                                .termsId(mt.getTerms().getId())
                                .agreedVersion(mt.getAgreedVersion())
                                .agreedAt(mt.getUpdatedAt())
                                .build())
                        .toList())
                .build();
    }

    // DTO -> entity
    public static MemberTerms toMemberTerms(Member member, Terms terms, boolean isAgreed, String agreedVersion) {
        return MemberTerms.create(
                member,
                terms,
                isAgreed,
                agreedVersion
        );
    }
}
