package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "목표 생성 응답")
public record GoalCreateResponseDto(

        @Schema(description = "생성된 목표 ID", example = "10")
        Long goalId,

        @Schema(description = "목표 제목", example = "여행 자금 모으기")
        String title,

        @Schema(description = "목표 금액", example = "1000000")
        Long targetAmount,

        @Schema(description = "시작 금액", example = "1000")
        Long startAmount,

        @Schema(description = "시작일", example = "2026-01-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2026-03-01")
        LocalDate endDate,

        @Schema(description = "남은 일수", example = "30")
        Long remainingDays,

        @Schema(description = "연결 계좌 정보")
        AccountDto account,

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
