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

    // 회원가입 시 약관 동의 내역 저장
    @Transactional
    public void saveTermsForRegistration(Member member, List<TermsRequestDTO.Agreement> agreements) {
        List<Terms> mandatoryTerms = termsRepository.findAllByIsActiveTrueAndIsRequiredTrue();
        validateMandatoryTerms(mandatoryTerms, agreements);

        // 약관 저장 공통 로직
        processMemberTermsUpsert(member, agreements);
    }

    // 기존 회원의 약관 동의 내역 저장
    @Transactional
    public void updateMemberTerms(Long memberId, List<TermsRequestDTO.Agreement> agreements) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 약관 저장 공통 로직
        processMemberTermsUpsert(member, agreements);
    }

    // 약관 저장 공통 로직
    private void processMemberTermsUpsert(Member member, List<TermsRequestDTO.Agreement> agreements) {
        // 요청 agreements에서 termsId만 추출
        List<Long> termsIds = agreements.stream()
                .map(TermsRequestDTO.Agreement::termsId)
                .distinct()
                .toList();

        // Terms를 리스트로 조회
        List<Terms> termsList = termsRepository.findAllById(termsIds);

        Map<Long, Terms> termsMap = termsList.stream()
                .collect(Collectors.toMap(t -> t.getId(), Function.identity()));

        // MemberTerms를 리스트로 조회
        List<MemberTerms> memberTermsList = memberTermsRepository.findAllByMemberIdAndTermsIdInWithTerms(member.getId(), termsIds);

        Map<Long, MemberTerms> memberTermsMap = memberTermsList.stream()
                .collect(Collectors.toMap(mt -> mt.getTerms().getId(), Function.identity()));

        for (TermsRequestDTO.Agreement agreement : agreements) {
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

    // 회원가입 시 필수 약관에 동의했는지 검증
    private void validateMandatoryTerms(List<Terms> mandatoryTerms, List<TermsRequestDTO.Agreement> agreements) {
        for (Terms mandatory : mandatoryTerms) {
            boolean agreed = agreements.stream()
                    .filter(a -> a.termsId().equals(mandatory.getId()))
                    .map(TermsRequestDTO.Agreement::isAgreed)
                    .findFirst()
                    .orElse(false);

            if (!agreed) {
                throw new TermsException(TermsErrorCode.MANDATORY_TERMS_NOT_AGREED);
            }
        }
    }
}
