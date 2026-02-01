package org.umc.valuedi.domain.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class KakaoResDTO {

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenInfoDTO {
        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfoDTO {
        private Long id;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KakaoAccount {
            private String name;
            private String birthyear;
            private String birthday;
            private String gender;
        }
    }

    public record UserTokenInfo(
            KakaoResDTO.UserInfoDTO userInfo,
            String accessToken
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserServiceTerms(
            Long id,
            @JsonProperty("service_terms")
            List<ServiceTerm> serviceTerms
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceTerm(
        String tag,
        Boolean agreed
    ) {}
}
