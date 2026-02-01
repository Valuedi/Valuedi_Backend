package org.umc.valuedi.domain.mbti.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "금융 MBTI 테스트 제출 요청 DTO")
public record FinanceMbtiTestRequestDto(
        @Schema(
                description = "문항 답변 리스트",
                example = """
                [
                  { "questionId": 1,  "choiceValue": 4 },
                  { "questionId": 2,  "choiceValue": 5 },
                  { "questionId": 3,  "choiceValue": 4 },
                  { "questionId": 4,  "choiceValue": 2 },
                  { "questionId": 5,  "choiceValue": 3 },
                  { "questionId": 6,  "choiceValue": 2 },
                  { "questionId": 7,  "choiceValue": 5 },
                  { "questionId": 8,  "choiceValue": 4 },
                  { "questionId": 9,  "choiceValue": 5 },
                  { "questionId": 10, "choiceValue": 3 },
                  { "questionId": 11, "choiceValue": 2 },
                  { "questionId": 12, "choiceValue": 3 }
                ]
                """
        )
        @NotEmpty List<Answer> answers
) {
    public record Answer(
            @Schema(description = "문항 ID", example = "1")
            @NotNull Long questionId,

            @Schema(description = "선택 값(1~5)", example = "4", minimum = "1", maximum = "5")
            @NotNull @Min(1) @Max(5) Integer choiceValue
    ) {}
}
