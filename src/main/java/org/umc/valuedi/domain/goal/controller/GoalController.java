package org.umc.valuedi.domain.goal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalActiveCountResponseDto;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.enums.GoalSort;
import org.umc.valuedi.domain.goal.exception.code.GoalSuccessCode;
import org.umc.valuedi.domain.goal.service.command.GoalCommandService;
import org.umc.valuedi.domain.goal.service.query.GoalQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController implements GoalControllerDocs{

    private final GoalCommandService goalService;
    private final GoalQueryService goalQueryService;

    // 목표 추가
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoalCreateResponseDto> createGoal(
            @RequestBody @Valid GoalCreateRequestDto req
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_CREATED,
                goalService.createGoal(req)
        );
    }

    // 전체 목표 조회 (진행/완료/취소 분리)
    @GetMapping
    public ApiResponse<GoalListResponseDto> getGoals(
            @RequestParam Long memberId,
            @RequestParam GoalStatus status,
            @RequestParam(defaultValue = "CREATED_AT_DESC") GoalSort sort,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_LIST_FETCHED,
                goalQueryService.getGoals(memberId, status, sort, limit)
        );
    }

    // 목표 상세 조회
    @GetMapping("/{goalId}")
    public ApiResponse<GoalDetailResponseDto> getGoalDetail(
            @PathVariable Long goalId
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_DETAIL_FETCHED,
                goalQueryService.getGoalDetail(goalId)
        );
    }

    // 목표 수정
    @PatchMapping("/{goalId}")
    public ApiResponse<Void> updateGoal(
            @PathVariable Long goalId,
            @RequestBody @Valid GoalUpdateRequestDto req
    ) {
        goalService.updateGoal(goalId, req);
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_UPDATED,
                null
        );
    }

    // 목표 삭제
    @DeleteMapping("/{goalId}")
    public ApiResponse<Void> deleteGoal(
            @PathVariable Long goalId
    ) {
        goalService.deleteGoal(goalId);
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_DELETED,
                null
        );
    }

    // 목표 개수 조회
    @GetMapping("/count")
    public ApiResponse<GoalActiveCountResponseDto> getActiveGoalCount(
            @RequestParam Long memberId
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_ACTIVE_COUNT_FETCHED,
                goalQueryService.getActiveGoalCount(memberId)
        );
    }
}
