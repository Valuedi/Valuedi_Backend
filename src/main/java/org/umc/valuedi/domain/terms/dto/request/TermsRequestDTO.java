package org.umc.valuedi.domain.terms.dto.request;

import java.util.List;

public class TermsRequestDTO {

    public record AgreeTermsRequest(
            List<Agreement> agreements
    ) {}

    public record Agreement(
            Long termsId,
            boolean isAgreed
    ) {}
}
