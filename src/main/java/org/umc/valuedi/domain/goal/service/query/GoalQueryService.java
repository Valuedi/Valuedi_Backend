package org.umc.valuedi.domain.goal.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.goal.converter.GoalConverter;
import org.umc.valuedi.domain.goal.dto.response.GoalActiveCountResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.entity.Goal;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.enums.GoalSort;
import org.umc.valuedi.domain.goal.exception.GoalException;
import org.umc.valuedi.domain.goal.exception.code.GoalErrorCode;
import org.umc.valuedi.domain.goal.repository.GoalRepository;
import org.umc.valuedi.domain.goal.service.GoalAchievementRateService;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalQueryService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRateService achievementRateService;

    // limit 있는 경우
    private List<Goal> findGoalsByStatus(
            Long memberId,
            GoalStatus status,
            Pageable pageable
    ) {
        return switch (status) {
            case ACTIVE ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.ACTIVE, pageable);
            case COMPLETE ->
                    goalRepository.findAllByMember_IdAndStatusIn(
                            memberId,
                            List.of(GoalStatus.COMPLETE, GoalStatus.FAILED),
                            pageable
                    );
            case FAILED ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.FAILED, pageable);
        };
    }

    // limit 없는 전체 조회용
    private List<Goal> findGoalsByStatus(Long memberId, GoalStatus status) {
        return switch (status) {
            case ACTIVE ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.ACTIVE);
            case COMPLETE ->
                    goalRepository.findAllByMember_IdAndStatusIn(
                            memberId,
                            List.of(GoalStatus.COMPLETE, GoalStatus.FAILED)
                    );
            case FAILED ->
                    goalRepository.findAllByMember_IdAndStatus(memberId, GoalStatus.FAILED);
        };
    }

    private List<GoalListResponseDto.GoalSummaryDto> toSummaryDtos(List<Goal> goals) {
        long savedAmount = 0; // 계좌 연동 후 수정

        return goals.stream()
                .map(g -> GoalConverter.toSummaryDto(
                        g,
                        savedAmount,
                        achievementRateService.calculateRate(savedAmount, g.getTargetAmount())
                ))
                .toList();
    }



    // 목표 전체 목록 조회
    public GoalListResponseDto getGoals(Long memberId, GoalStatus status, GoalSort sort, Integer limit) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        Integer size = (limit == null) ? null : Math.max(3, limit);
        GoalSort sortType = (sort == null) ? GoalSort.CREATED_AT_DESC : sort;

        List<Goal> goals;

        if (sortType == GoalSort.CREATED_AT_DESC) {
            if (size != null) {
                Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                goals = findGoalsByStatus(memberId, status, pageable);
            } else {
                goals = findGoalsByStatus(memberId, status).stream()
                        .sorted(Comparator.comparing(Goal::getCreatedAt).reversed())
                        .toList();
            }

            return new GoalListResponseDto(toSummaryDtos(goals));
        }
        // 달성률 높은순
        else {
            goals = findGoalsByStatus(memberId, status);

            var dtos = toSummaryDtos(goals).stream()
                    .sorted(Comparator.comparingInt(GoalListResponseDto.GoalSummaryDto::achievementRate).reversed());

            if (size != null) {
                dtos = dtos.limit(size);
            }

            return new GoalListResponseDto(dtos.toList());
        }
    }


    // 목표 상세 조회
    public GoalDetailResponseDto getGoalDetail(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new GoalException(GoalErrorCode.GOAL_NOT_FOUND));

        long savedAmount = 0; // 계좌 연동 후 수정
        int rate = achievementRateService.calculateRate(savedAmount, goal.getTargetAmount());

        return GoalConverter.toDetailDto(goal, savedAmount, rate);
    }


    // 목표 개수 조회
    public GoalActiveCountResponseDto getActiveGoalCount(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        long count = goalRepository.countByMember_IdAndStatus(memberId, GoalStatus.ACTIVE);
        return new GoalActiveCountResponseDto((int) count);
    }
}
