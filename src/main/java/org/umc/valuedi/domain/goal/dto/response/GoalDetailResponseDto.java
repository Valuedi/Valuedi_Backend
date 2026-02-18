package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.umc.valuedi.domain.goal.enums.GoalStatus;

import java.time.LocalDate;

@Schema(description = "목표 상세 조회 응답")
public record GoalDetailResponseDto(

        @Schema(description = "목표 ID", example = "10")
        Long goalId,

        @Schema(description = "목표 제목", example = "여행 자금 모으기")
        String title,

        @Schema(description = "현재 계좌 잔액", example = "300000")
        Long currentBalance,

        @Schema(description = "목표 금액", example = "1000000")
        Long targetAmount,

        @Schema(description = "시작일", example = "2024-01-01")
        LocalDate startDate,

        @Schema(description = "목표일", example = "2024-12-31")
        LocalDate endDate,

        @Schema(description = "남은 일수", example = "30")
        Long remainingDays,

        @Schema(description = "달성률(0~100)", example = "30")
        Integer achievementRate, // 0~100

        @Schema(description = "연결 계좌 정보")
        AccountDto account,

        @Schema(description = "목표 상태", example = "ACTIVE")
        GoalStatus status,

        @Schema(description = "색상 코드(# 제외)", example = "FF6363")
        String colorCode,

        @Schema(description = "아이콘 ID", example = "3")
        Integer iconId

) {
    @Schema(description = "목표와 연결된 계좌 정보")
    public record AccountDto(

            @Schema(description = "은행명", example = "신한은행")
            String bankName,

            @Schema(description = "계좌번호", example = "110-123-456789")
            String accountNumber
    ) {}
}
