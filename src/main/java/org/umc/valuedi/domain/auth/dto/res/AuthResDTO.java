package org.umc.valuedi.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

public class AuthResDTO {

    @Builder
    public record LoginResultDTO (
            String accessToken,

            // 리프레시 토큰은 쿠키로 전달하고 JSON body에는 저장하지 않음
            @JsonIgnore
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
}
