package org.umc.valuedi.domain.mbti.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FinanceMbtiTestRequestDto(
        @NotNull Long memberId,
        @NotEmpty List<Answer> answers
) {
    public record Answer(
            @NotNull Long questionId,
            @NotNull @Min(1) @Max(5) Byte choiceValue
    ) {}
}
