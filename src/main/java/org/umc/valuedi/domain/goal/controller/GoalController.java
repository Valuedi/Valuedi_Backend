package org.umc.valuedi.domain.goal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalLinkAccountRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.*;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.enums.GoalSort;
import org.umc.valuedi.domain.goal.exception.code.GoalSuccessCode;
import org.umc.valuedi.domain.goal.service.command.GoalAccountCommandService;
import org.umc.valuedi.domain.goal.service.command.GoalCommandService;
import org.umc.valuedi.domain.goal.service.query.GoalAccountQueryService;
import org.umc.valuedi.domain.goal.service.query.GoalListQueryService;
import org.umc.valuedi.domain.goal.service.query.GoalQueryService;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController implements GoalControllerDocs{

    private final GoalCommandService goalService;
    private final GoalQueryService goalQueryService;
    private final GoalListQueryService goalListQueryService;
    private final GoalAccountQueryService goalAccountQueryService;
    private final GoalAccountCommandService goalAccountCommandService;

    // 목표 추가
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoalCreateResponseDto> createGoal(
            @CurrentMember Long memberId,
            @RequestBody @Valid GoalCreateRequestDto req
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_CREATED,
                goalService.createGoal(memberId, req)
        );
    }

    // 전체 목표 조회 (진행/완료/취소 분리)
    @GetMapping
    public ApiResponse<GoalListResponseDto> getGoals(
            @CurrentMember Long memberId,
            @RequestParam(defaultValue = "ACTIVE") GoalStatus status,
            @RequestParam(defaultValue = "TIME_DESC") GoalSort sort,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_LIST_FETCHED,
                goalListQueryService.getGoals(memberId, status, sort, limit)
        );
    }

    // 목표 상세 조회
    @GetMapping("/{goalId}")
    public ApiResponse<GoalDetailResponseDto> getGoalDetail(
            @CurrentMember Long memberId,
            @PathVariable Long goalId
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_DETAIL_FETCHED,
                goalQueryService.getGoalDetail(memberId, goalId)
        );
    }

    // 목표 수정
    @PatchMapping("/{goalId}")
    public ApiResponse<Void> updateGoal(
            @CurrentMember Long memberId,
            @PathVariable Long goalId,
            @RequestBody @Valid GoalUpdateRequestDto req
    ) {
        goalService.updateGoal(memberId, goalId, req);
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_UPDATED,
                null
        );
    }

    // 목표 삭제
    @DeleteMapping("/{goalId}")
    public ApiResponse<Void> deleteGoal(
            @CurrentMember Long memberId,
            @PathVariable Long goalId
    ) {
        goalService.deleteGoal(memberId, goalId);
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_DELETED,
                null
        );
    }

    // 목표 개수 조회
    @GetMapping("/count")
    public ApiResponse<GoalActiveCountResponseDto> getActiveGoalCount(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_ACTIVE_COUNT_FETCHED,
                goalQueryService.getActiveGoalCount(memberId)
        );
    }


    // 목표 없는 계좌 조회
    @GetMapping("/accounts")
    public ApiResponse<GoalAccountResDto.UnlinkedBankAccountListDTO> getUnlinkedAccounts(
            @CurrentMember Long memberId

    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_UNLINKED_ACCOUNTS_FETCHED,
                goalAccountQueryService.getUnlinkedAccounts(memberId)
        );
    }

    // 목표에 계좌 재연결
    @PutMapping("/{goalId}/linked-accounts")
    public ApiResponse<Void> linkAccountToGoal(
            @CurrentMember Long memberId,
            @PathVariable Long goalId,
            @RequestBody @Valid GoalLinkAccountRequestDto req
    ) {
        goalAccountCommandService.setLinkedAccount(memberId, goalId, req.accountId());
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_ACCOUNT_LINKED,
                null
        );
    }

    // 주요 목표 조회(홈화면)
    @GetMapping("/primary")
    public ApiResponse<GoalPrimaryListResponseDto> getPrimaryGoals(
            @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(
                GoalSuccessCode.GOAL_LIST_FETCHED,
                goalQueryService.getPrimaryGoals(memberId)
        );
    }
}
