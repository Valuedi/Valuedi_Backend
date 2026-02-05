package org.umc.valuedi.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Builder
    public record AuthStatusDTO (
            Boolean isLogin,
            Long memberId
    ) {}

    @Builder
    public record LoginUrlDTO(
            String url,
            String state
    ) {}
}
