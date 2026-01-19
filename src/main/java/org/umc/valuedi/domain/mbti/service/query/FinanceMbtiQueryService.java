package org.umc.valuedi.domain.mbti.service.query;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.mbti.dto.FinanceMbtiTypeInfoDto;
import org.umc.valuedi.domain.mbti.entity.MbtiQuestion;
import org.umc.valuedi.domain.mbti.entity.MemberMbtiTest;
import org.umc.valuedi.domain.mbti.exception.MbtiException;
import org.umc.valuedi.domain.mbti.exception.code.MbtiErrorCode;
import org.umc.valuedi.domain.mbti.repository.MbtiQuestionRepository;
import org.umc.valuedi.domain.mbti.repository.MemberMbtiTestRepository;
import org.umc.valuedi.domain.mbti.service.FinanceMbtiScoringService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceMbtiQueryService {

    private final MemberMbtiTestRepository memberMbtiTestRepository;
    private final MbtiQuestionRepository mbtiQuestionRepository;
    private final FinanceMbtiScoringService scoringService;

    public List<MbtiQuestion> getActiveQuestions() {
        return mbtiQuestionRepository.findAllByOrderByIdAsc();
    }

    public MemberMbtiTest getCurrentResult(Long memberId) {
        return memberMbtiTestRepository.findCurrentActiveTest(memberId)
                .orElseThrow(() -> new MbtiException(MbtiErrorCode.NO_ACTIVE_RESULT));
    }

    public List<FinanceMbtiTypeInfoDto> getTypeInfos() {
        return scoringService.getTypeInfos();
    }
}
