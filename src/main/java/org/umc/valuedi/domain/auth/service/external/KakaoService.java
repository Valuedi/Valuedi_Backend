package org.umc.valuedi.domain.auth.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.feign.KakaoApiClient;
import org.umc.valuedi.domain.auth.feign.KakaoAuthClient;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;

    public KakaoResDTO.UserInfoDTO getKakaoUserInfo(String code) {
        KakaoResDTO.TokenInfoDTO tokenInfo = kakaoAuthClient.getKakaoToken(
                "authorization_code",
                kakaoProperties.getClientId(),
                kakaoProperties.getRedirectUri(),
                code,
                kakaoProperties.getClientSecret()
        );

        return kakaoApiClient.getUserInfo(
                tokenInfo.getTokenType() + " " + tokenInfo.getAccessToken()
        );
    }
}
