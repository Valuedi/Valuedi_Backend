package org.umc.valuedi.domain.auth.dto.res;

import lombok.*;

public class AuthResDTO {

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class LoginResultDTO {
        private String accessToken;
        private String refreshToken;
        private Long memberId;
    }
}
