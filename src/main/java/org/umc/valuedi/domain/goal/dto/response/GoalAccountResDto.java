package org.umc.valuedi.domain.goal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "목표와 연결되지 않은 계좌 목록 조회 응답")
public class GoalAccountResDto {

    @Schema(description = "목표와 연결되지 않은 단일 계좌 정보")
    public record UnlinkedBankAccountDTO(

            @Schema(description = "계좌 ID", example = "12")
            Long accountId,

            @Schema(description = "계좌명", example = "입출금통장")
            String accountName,

            @Schema(description = "표시용 계좌번호(마스킹 포함 가능)", example = "110-****-1234")
            String accountDisplay
    ) {}

    @Schema(description = "목표와 연결되지 않은 계좌 목록")
    public record UnlinkedBankAccountListDTO(

            @Schema(description = "계좌 목록")
            List<UnlinkedBankAccountDTO> accounts
    ) {}
}
