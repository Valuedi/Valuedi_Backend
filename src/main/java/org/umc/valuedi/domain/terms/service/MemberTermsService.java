package org.umc.valuedi.domain.terms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.terms.converter.TermsConverter;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.domain.terms.entity.MemberTerms;
import org.umc.valuedi.domain.terms.entity.Terms;
import org.umc.valuedi.domain.terms.exception.TermsException;
import org.umc.valuedi.domain.terms.exception.code.TermsErrorCode;
import org.umc.valuedi.domain.terms.repository.MemberTermsRepository;
import org.umc.valuedi.domain.terms.repository.TermsRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTermsService {

    private final MemberTermsRepository memberTermsRepository;
    private final MemberRepository memberRepository;
    private final TermsRepository termsRepository;

    // 사용자가 동의한 약관 조회
    public TermsResponseDTO.GetMemberAgreements getMemberAgreements(Long memberId) {
        // MemberTerms 목록 조회
        List<MemberTerms> memberTermsList = memberTermsRepository.findAgreedByMemberIdWithTerms(memberId);

        // DTO 변환 후 반환
        return TermsConverter.toGetMemberAgreementsDTO(memberTermsList);
    }

    // 약관 동의 저장
    @Transactional
    public void saveAgreeTerms(Long memberId, TermsRequestDTO.AgreeTermsRequest dto) {
        // Member 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 요청 agreements에서 termsId만 추출
        List<Long> termsIds = dto.agreements().stream()
                .map(TermsRequestDTO.Agreement::termsId)
                .distinct()
                .toList();

        if (termsIds.isEmpty()) {
            throw new TermsException(TermsErrorCode.TERMS_NOT_FOUND);
        }

        // Terms를 리스트로 조회
        List<Terms> termsList = termsRepository.findAllById(termsIds);

        Map<Long, Terms> termsMap = termsList.stream()
                .collect(Collectors.toMap(t -> t.getId(), Function.identity()));

        // MemberTerms를 리스트로 조회
        List<MemberTerms> memberTermsList = memberTermsRepository.findAllByMemberIdAndTermsIdInWithTerms(memberId, termsIds);

        Map<Long, MemberTerms> memberTermsMap = memberTermsList.stream()
                .collect(Collectors.toMap(mt -> mt.getTerms().getId(), Function.identity()));

        for (TermsRequestDTO.Agreement agreement : dto.agreements()) {
            Long termsId = agreement.termsId();
            Terms terms = termsMap.get(termsId);

            if (terms == null) {
                throw new TermsException(TermsErrorCode.TERMS_NOT_FOUND);
            }

            String agreedVersion = terms.getVersion();
            MemberTerms memberTerms = memberTermsMap.get(termsId);

            if (memberTerms == null) {
                MemberTerms newMemberTerms = TermsConverter.toMemberTerms(member, terms, agreement.isAgreed(), agreedVersion);
                memberTermsRepository.save(newMemberTerms);

                memberTermsMap.put(termsId, newMemberTerms);
            } else {
                memberTerms.updateAgreement(agreement.isAgreed(), agreedVersion);
            }
        }
    }
}
