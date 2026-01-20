package org.umc.valuedi.domain.terms.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class TermsResponseDTO {

    @Builder
    public record GetTermsList(
            List<TermsList> termsLists
    ) {}

    @Builder
    public record TermsList(
            Long termsId,
            String code,
            String title,
            boolean isRequired
    ) {}

    @Builder
    public record GetMemberAgreements(
            List<MemberAgreement> agreements
    ) {}

    @Builder
    public record MemberAgreement(
            Long termsId,
            String agreedVersion,
            LocalDateTime agreedAt
    ) {}
}
