package org.umc.valuedi.global.external.genai.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class GeminiSavingsResponseDTO {

    public record Result(List<Item> recommendations) {}

    public record Item(
            String finPrdtCd,
            Long optionId,
            BigDecimal score,
            List<Reason> reasons
    ) {}

    public record Reason(
            String reasonCode,
            String reasonText,
            BigDecimal delta
    ) {}
}
