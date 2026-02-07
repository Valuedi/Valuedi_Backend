package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.service.AssetBalanceService;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalSort;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.service.GoalAchievementRateService;
import org.umc.valuedi.domain.goal.service.GoalStatusChangeService;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalListQueryService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRateService achievementRateService;
    private final AssetBalanceService assetBalanceService;
    private final GoalStatusChangeService goalStatusChangeService;

    private void validateListStatus(GoalStatus status) {
        if (status != GoalStatus.ACTIVE && status != GoalStatus.COMPLETE) {
            throw new GoalException(GoalErrorCode.INVALID_GOAL_LIST_STATUS);
        }
    }

    // limit 있을 때
    private List<Goal> findGoals(Long memberId, GoalStatus status, Pageable pageable) {
        validateListStatus(status);

        return switch (status) {
            case ACTIVE ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.ACTIVE, pageable);
            case COMPLETE ->
                    goalRepository.findAllByMember_IdAndStatusIn(
                            memberId,
                            List.of(GoalStatus.COMPLETE, GoalStatus.FAILED),
                            pageable
                    );
            default -> throw new GoalException(GoalErrorCode.INVALID_GOAL_LIST_STATUS);
        };
    }

    // limit 없을 때
    private List<Goal> findGoals(Long memberId, GoalStatus status) {
        validateListStatus(status);

        return switch (status) {
            case ACTIVE ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.ACTIVE);
            case COMPLETE ->
                    goalRepository.findAllByMember_IdAndStatusIn(
                            memberId,
                            List.of(GoalStatus.COMPLETE, GoalStatus.FAILED)
                    );
            default -> throw new GoalException(GoalErrorCode.INVALID_GOAL_LIST_STATUS);
        };
    }

    private List<GoalListResponseDto.GoalSummaryDto> toSummaryDtos(List<Goal> goals, Long memberId) {

        return goals.stream()
                .map(g -> {
                    BankAccount account = g.getBankAccount();

                    if (!account.getIsActive()) {
                        throw new GoalException(GoalErrorCode.GOAL_ACCOUNT_INACTIVE);
                    }

                    // 동기화 후 최신 잔액 가져오기
                    Long currentBalance = assetBalanceService.syncAndGetLatestBalance(memberId, account.getId());

                    // 현재 잔액 - 시작 잔액
                    long savedAmount = currentBalance - g.getStartAmount();

                    // 목표 달성 여부 체크 및 상태 업데이트 (공통 로직 사용)
                    goalStatusChangeService.checkAndUpdateStatus(g, savedAmount);
                    
                    int rate = achievementRateService.calculateRate(savedAmount, g.getTargetAmount());

                    return GoalConverter.toSummaryDto(g, savedAmount, rate);
                })
                .toList();
    }

    private String resolveTimeSortField(GoalStatus status) {
        // ACTIVE: 생성 최신순 / COMPLETE: 종료 최신순
        return (status == GoalStatus.ACTIVE) ? "createdAt" : "completedAt";
    }

    private Comparator<Goal> resolveTimeComparator(GoalStatus status) {
        if (status == GoalStatus.ACTIVE) {
            return Comparator.comparing(Goal::getCreatedAt).reversed();
        }
        return Comparator.comparing(
                        Goal::getCompletedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
                .reversed();
    }

    // 목표 전체 목록
    @Transactional // 상태 변경이 일어날 수 있으므로 트랜잭션 추가
    public GoalListResponseDto getGoals(Long memberId, GoalStatus status, GoalSort sort, Integer limit) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        GoalStatus listStatus = (status == null) ? GoalStatus.ACTIVE : status;
        validateListStatus(listStatus);

        GoalSort sortType = (sort == null) ? GoalSort.TIME_DESC : sort;
        Integer size = (limit == null) ? null : Math.max(3, limit);

        // COMPLETE + PROGRESS_DESC => 성공한 목표만 + 달성(완료)된 순
        if (listStatus == GoalStatus.COMPLETE && sortType == GoalSort.PROGRESS_DESC) {
            List<Goal> goals = goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.COMPLETE).stream()
                    .sorted(Comparator.comparing(
                                    Goal::getCompletedAt,
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )
                            .reversed())
                    .toList();

            if (size != null && goals.size() > size) {
                goals = goals.subList(0, size);
            }

            return new GoalListResponseDto(toSummaryDtos(goals, memberId));
        }

        // TIME_DESC (ACTIVE면 createdAt, COMPLETE면 completedAt)
        if (sortType == GoalSort.TIME_DESC) {
            String timeField = resolveTimeSortField(listStatus);

            List<Goal> goals;
            if (size != null) {
                Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, timeField));
                goals = findGoals(memberId, listStatus, pageable);
            } else {
                goals = findGoals(memberId, listStatus).stream()
                        .sorted(resolveTimeComparator(listStatus))
                        .toList();
            }

            return new GoalListResponseDto(toSummaryDtos(goals, memberId));
        }

        // ACTIVE + PROGRESS_DESC => 달성률 높은 진행 중인 목표만
        List<Goal> goals = findGoals(memberId, listStatus);
        var dtos = toSummaryDtos(goals, memberId).stream()
                .sorted(Comparator.comparingInt(GoalListResponseDto.GoalSummaryDto::achievementRate).reversed());

        if (size != null) {
            dtos = dtos.limit(size);
        }

        return new GoalListResponseDto(dtos.toList());
    }
}
