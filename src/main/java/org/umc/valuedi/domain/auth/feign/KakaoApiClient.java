package org.umc.valuedi.domain.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;

@FeignClient(name = "KakaoApiClient", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @GetMapping(value = "/v2/user/me", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    KakaoResDTO.UserInfoDTO getUserInfo(
            @RequestHeader("Authorization") String accessToken
    );

    @GetMapping(value = "/v2/user/service_terms")
    KakaoResDTO.UserServiceTerms getServiceTerms(
            @RequestHeader("Authorization") String accessToken
    );

}
