package org.umc.valuedi.domain.auth.dto.res;

import lombok.*;

public class AuthResDTO {

    @Builder
    public record LoginResultDTO (
        String accessToken,
        String refreshToken,
        Long memberId
    ) {}

    @Builder
    public record RegisterResDTO (
            Long memberId
    ) {}
}
