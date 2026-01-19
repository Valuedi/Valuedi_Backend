package org.umc.valuedi.domain.goal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.umc.valuedi.domain.goal.dto.request.GoalCreateRequestDto;
import org.umc.valuedi.domain.goal.dto.request.GoalUpdateRequestDto;
import org.umc.valuedi.domain.goal.dto.response.GoalCreateResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalDetailResponseDto;
import org.umc.valuedi.domain.goal.dto.response.GoalListResponseDto;
import org.umc.valuedi.domain.goal.enums.GoalStatus;
import org.umc.valuedi.domain.goal.service.GoalService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    // 목표 추가
    @PostMapping
    public ResponseEntity<GoalCreateResponseDto> createGoal(@RequestBody @Valid GoalCreateRequestDto req) {
        return ResponseEntity.ok(goalService.createGoal(req));
    }

    // 전체 목표 조회 (진행/완료/취소 분리)
    @GetMapping
    public ResponseEntity<GoalListResponseDto> getGoals(
            @RequestParam Long memberId,
            @RequestParam GoalStatus status
    ) {
        return ResponseEntity.ok(goalService.getGoals(memberId, status));
    }

    // 목표 상세 조회
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDto> getGoalDetail(@PathVariable Long goalId) {
        return ResponseEntity.ok(goalService.getGoalDetail(goalId));
    }

    // 목표 수정
    @PatchMapping("/{goalId}")
    public ResponseEntity<Void> updateGoal(
            @PathVariable Long goalId,
            @RequestBody @Valid GoalUpdateRequestDto req
    ) {
        goalService.updateGoal(goalId, req);
        return ResponseEntity.ok().build();
    }

    // 목표 삭제
    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok().build();
    }
}
