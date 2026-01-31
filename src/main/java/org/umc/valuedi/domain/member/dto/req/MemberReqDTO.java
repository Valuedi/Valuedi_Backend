package org.umc.valuedi.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.umc.valuedi.domain.member.enums.WithdrawalReason;

public class MemberReqDTO {

    public record MemberWithdrawDTO(
            @NotNull(message = "탈퇴 사유를 선택해주세요.")
            @Schema(description = "탈퇴 사유", example = "NOT_HELPFUL")
            WithdrawalReason reason
    ) {}
}
