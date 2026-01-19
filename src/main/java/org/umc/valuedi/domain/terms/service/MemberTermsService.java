package org.umc.valuedi.domain.terms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.terms.converter.TermsConverter;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.entity.MemberTerms;
import org.umc.valuedi.domain.terms.repository.MemberTermsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTermsService {

    private final MemberTermsRepository memberTermsRepository;

    // 사용자가 동의한 약관 조회
    public TermsResponseDTO.GetMemberAgreements getMemberAgreements(Long memberId) {
        // MemberTerms 목록 조회
        List<MemberTerms> memberTermsList = memberTermsRepository.findAgreedByMemberIdWithTerms(memberId);

        // DTO 변환 후 반환
        return TermsConverter.toGetMemberAgreementsDTO(memberTermsList);
    }
}
