package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.ledger.converter.LedgerConverter;
import org.umc.valuedi.domain.ledger.dto.response.LedgerListResponse;
import org.umc.valuedi.domain.ledger.entity.LedgerEntry;
import org.umc.valuedi.domain.ledger.repository.LedgerQueryRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalLedgerQueryService {

    private final GoalRepository goalRepository;
    private final LedgerQueryRepository ledgerQueryRepository;

    public LedgerListResponse getGoalLedgerTransactions(Long memberId, Long goalId, int page, int size) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        if (!goal.getMember().getId().equals(memberId)) {
            throw new GoalException(GoalErrorCode.GOAL_FORBIDDEN);
        }

        // 시작 시간: 목표 생성 시각 (createdAt)
        LocalDateTime from = goal.getCreatedAt();
        if (goal.getStartDate().atStartOfDay().isAfter(from)) {
            from = goal.getStartDate().atStartOfDay();
        }

        LocalDateTime to = goal.getEndDate().atTime(LocalTime.MAX);

        Page<LedgerEntry> result = ledgerQueryRepository.searchByPeriodLatest(
                memberId,
                from,
                to,
                PageRequest.of(page, size)
        );

        return LedgerConverter.toLedgerListResponse(
                result.getContent(),
                page,
                size,
                result.getTotalElements()
        );
    }
}
