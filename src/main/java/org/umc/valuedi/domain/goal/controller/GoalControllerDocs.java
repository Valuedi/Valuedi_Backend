package org.umc.valuedi.domain.goal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalLinkAccountRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.*;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.enums.GoalSort;
import org.umc.valuedi.global.security.annotation.CurrentMember;

@Tag(name = "Goal", description = "목표(Goal) 생성/조회/수정/삭제 API")
public interface GoalControllerDocs {

    @Operation(
            summary = "목표 추가 API",
            description = "로그인한 사용자가 목표를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (날짜 범위 오류, targetAmount 범위 오류 등)"),
            @ApiResponse(responseCode = "404", description = "회원 또는 계좌가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalCreateResponseDto> createGoal(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody GoalCreateRequestDto req
    );

    @Operation(
            summary = "목표 목록 조회 API",
            description = "로그인한 사용자의 목표 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "status/sort 파라미터 오류"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalListResponseDto> getGoals(
            @Parameter(hidden = true) @CurrentMember Long memberId,

            @Parameter(description = "목표 상태", example = "ACTIVE")
            @RequestParam(defaultValue = "ACTIVE") GoalStatus status,

            @Parameter(description = "목표 정렬", example = "TIME_DESC")
            @RequestParam(defaultValue = "TIME_DESC") GoalSort sort,

            @Parameter(description = "표시할 목표 수(없으면 전체)", example = "3")
            @RequestParam(required = false) Integer limit
    );

    @Operation(
            summary = "목표 상세 조회 API",
            description = "로그인한 사용자의 특정 목표(goalId)에 대한 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "목표가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalDetailResponseDto> getGoalDetail(
            @Parameter(hidden = true) @CurrentMember Long memberId,

            @Parameter(description = "목표 ID", example = "10", required = true)
            @PathVariable Long goalId
    );

    @Operation(
            summary = "목표 수정 API",
            description = "로그인한 사용자의 특정 목표(goalId)의 제목/기간/금액 등을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "검증 실패 (날짜 범위 오류, targetAmount 범위 오류 등)"),
            @ApiResponse(responseCode = "404", description = "목표가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<Void> updateGoal(
            @Parameter(hidden = true) @CurrentMember Long memberId,

            @Parameter(description = "목표 ID", example = "10", required = true)
            @PathVariable Long goalId,

            @Valid @RequestBody GoalUpdateRequestDto req
    );

    @Operation(
            summary = "목표 삭제 API",
            description = "로그인한 사용자의 특정 목표(goalId)를 삭제(soft delete) 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "목표가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<Void> deleteGoal(
            @Parameter(hidden = true) @CurrentMember Long memberId,

            @Parameter(description = "목표 ID", example = "10", required = true)
            @PathVariable Long goalId
    );

    @Operation(
            summary = "진행 중인 목표 개수 조회 API",
            description = "로그인한 사용자의 현재 진행 중(ACTIVE) 목표 개수를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalActiveCountResponseDto> getActiveGoalCount(
            @Parameter(hidden = true) @CurrentMember Long memberId
    );

    @Operation(
            summary = "목표에 연결되지 않은 계좌 목록 조회 API",
            description = "로그인한 사용자의 계좌 중 아직 어떤 목표에도 연결되지 않은 계좌 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalAccountResDto.UnlinkedBankAccountListDTO> getUnlinkedAccounts(
            @Parameter(hidden = true) @CurrentMember Long memberId
    );

    @Operation(
            summary = "목표-계좌 연결 API",
            description = "특정 목표(goalId)에 로그인한 사용자의 계좌(accountId)를 1:1로 연결합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연결 성공"),
            @ApiResponse(responseCode = "400", description = "이미 연결된 목표/계좌 등 요청 오류"),
            @ApiResponse(responseCode = "404", description = "목표 또는 계좌가 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<Void> linkAccountToGoal(
            @Parameter(hidden = true) @CurrentMember Long memberId,

            @Parameter(description = "목표 ID", example = "10", required = true)
            @PathVariable Long goalId,

            @Valid @RequestBody GoalLinkAccountRequestDto req
    );

    @Operation(
            summary = "홈화면 목표 목록 조회 API",
            description = "로그인한 사용자의 진행 중(ACTIVE) 목표를 생성일 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    org.umc.valuedi.global.apiPayload.ApiResponse<GoalPrimaryListResponseDto> getPrimaryGoals(
            @Parameter(hidden = true) @CurrentMember Long memberId
    );
}
