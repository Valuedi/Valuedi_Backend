package org.umc.valuedi.domain.mbti.validation;

import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTestRequestDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.exception.MbtiException;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FinanceMbtiTestValidator {

    public Map<Long, MbtiQuestion> validateAndBuildQuestionMap(
            FinanceMbtiTestRequestDto req,
            List<MbtiQuestion> activeQuestions
    ) {
        if (activeQuestions == null || activeQuestions.isEmpty()) {
            throw new MbtiException(MbtiErrorCode.NO_ACTIVE_QUESTIONS);
        }

        // 서버 활성 문항 Map 구성
        Map<Long, MbtiQuestion> activeQuestionMap = activeQuestions.stream()
                .collect(Collectors.toMap(MbtiQuestion::getId, Function.identity()));

        // 1) answers 중복 questionId 체크
        long distinctCount = req.answers().stream()
                .map(FinanceMbtiTestRequestDto.Answer::questionId)
                .distinct()
                .count();

        if (distinctCount != req.answers().size()) {
            throw new MbtiException(MbtiErrorCode.DUPLICATE_QUESTION_ID);
        }

        // 2) answers 개수 검증 (누락/초과 차단)
        if (req.answers().size() != activeQuestions.size()) {
            throw new MbtiException(MbtiErrorCode.NOT_ALL_ANSWERED);
        }

        // 3) questionId 유효성 + choiceValue 범위 검증
        for (FinanceMbtiTestRequestDto.Answer a : req.answers()) {

            if (!activeQuestionMap.containsKey(a.questionId())) {
                throw new MbtiException(MbtiErrorCode.INVALID_QUESTION_ID);
            }

            Integer choiceValue = a.choiceValue();
            if (choiceValue == null || choiceValue < 1 || choiceValue > 5) {
                throw new MbtiException(MbtiErrorCode.INVALID_CHOICE_VALUE);
            }
        }

        return activeQuestionMap;
    }
}