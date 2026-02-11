package org.umc.valuedi.global.external.kakao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.umc.valuedi.global.external.kakao.config.KakaoProperties;
import org.umc.valuedi.global.external.kakao.dto.res.KakaoResDTO;
import org.umc.valuedi.global.external.kakao.client.KakaoApiClient;
import org.umc.valuedi.global.external.kakao.client.KakaoAuthClient;
import org.umc.valuedi.domain.terms.dto.request.TermsRequestDTO;
import org.umc.valuedi.domain.terms.entity.Terms;
import org.umc.valuedi.domain.terms.repository.TermsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;
    private final TermsRepository termsRepository;

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

        Map<String, Boolean> kakaoAgreement = userServiceTerms.serviceTerms().stream()
                .collect(Collectors.toMap(
                        term -> "user_age_check".equals(term.tag()) ? "AGE_14" : term.tag(),
                        KakaoResDTO.ServiceTerm::agreed
                ));

        List<Terms> activeTerms = termsRepository.findAllByCodeInAndIsActiveTrue(
                new ArrayList<>(kakaoAgreement.keySet())
        );

        return activeTerms.stream()
                .map(term -> new TermsRequestDTO.Agreement(
                        term.getId(),
                        kakaoAgreement.get(term.getCode())
                ))
                .toList();
    }

    public void unlinkKakao(Long providerId) {
        String authHeader = "KakaoAK " + kakaoProperties.getAdminKey();

        try {
            kakaoApiClient.unlinkUser(authHeader, "user_id", providerId);
        } catch (feign.FeignException e) {
            /*
             사용자가 카카오 설정에서 직접 연결 해제 했을 경우 예외 발생할 수 있음.
             이 경우 에러 응답을 하는 것이 아니라 로그 남긴 후 회원 탈퇴 로직 계속 진행
             */
            log.error("카카오 연결 해제 API 호출 실패: status={}, message={}", e.status(), e.getMessage());
        }
    }
}
