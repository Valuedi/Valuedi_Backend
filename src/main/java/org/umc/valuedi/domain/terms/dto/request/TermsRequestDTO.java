package org.umc.valuedi.domain.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class TermsRequestDTO {

    public record AgreeTermsRequest(
            @NotEmpty(message = "약관 동의 내역은 최소 하나 이상 포함되어야 합니다.")
            @Schema(
                    description = "약관 동의 내역 리스트",
                    example = "[{\"termsId\": 1, \"isAgreed\": true}, {\"termsId\": 2, \"isAgreed\": false}]"
            )
            List<Agreement> agreements
    ) {}

    public record Agreement(
            @Schema(description = "약관 번호", example = "1")
            Long termsId,

            @Schema(description = "약관 동의 여부", example = "true")
            boolean isAgreed
    ) {}
}
