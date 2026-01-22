package org.umc.valuedi.domain.auth.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.auth.config.KakaoProperties;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.feign.KakaoApiClient;
import org.umc.valuedi.domain.auth.feign.KakaoAuthClient;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;

    public KakaoResDTO.UserTokenInfo getKakaoUserInfo(String code) {
        KakaoResDTO.TokenInfoDTO tokenInfo = kakaoAuthClient.getKakaoToken(
                "authorization_code",
                kakaoProperties.getClientId(),
                kakaoProperties.getRedirectUri(),
                code,
                kakaoProperties.getClientSecret()
        );

        String reqAccessToken = tokenInfo.getTokenType() + " " + tokenInfo.getAccessToken();
        KakaoResDTO.UserInfoDTO userInfo = kakaoApiClient.getUserInfo(reqAccessToken);

        return new KakaoResDTO.UserTokenInfo(userInfo, reqAccessToken);
    }

    public List<TermsRequestDTO.Agreement> getKakaoServiceTerms(String accessToken) {
        KakaoResDTO.UserServiceTerms userServiceTerms = kakaoApiClient.getServiceTerms(accessToken);

        return userServiceTerms.serviceTerms().stream()
                .map(term -> new TermsRequestDTO.Agreement(
                        mapKakaoTagToTermsId(term.tag()),
                        term.agreed()
                ))
                .toList();
    }

    // 카카오 약관 태그를 Terms 엔티티의 ID로 매핑하는 메서드
    private Long mapKakaoTagToTermsId(String kakaoTag) {
        // 추후 하드코딩 방식에서 DB 조회하는 방식으로 변경 예정
        return switch (kakaoTag) {
            case "user_age_check" -> 1L;
            case "SERVICE" -> 2L;
            case "PRIVACY" -> 3L;
            case "MARKETING" -> 4L;
            case "SECURITY" -> 5L;
            default -> 0L;
        };
    }
}
