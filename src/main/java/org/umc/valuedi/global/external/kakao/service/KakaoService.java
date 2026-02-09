package org.umc.valuedi.global.external.kakao.service;

import lombok.RequiredArgsConstructor;
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
}
