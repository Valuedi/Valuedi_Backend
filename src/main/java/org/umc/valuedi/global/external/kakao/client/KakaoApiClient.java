package org.umc.valuedi.global.external.kakao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.valuedi.global.external.kakao.dto.res.KakaoResDTO;

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

    @PostMapping(value = "/v1/user/unlink", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    void unlinkUser(
            @RequestHeader("Authorization") String adminKeyHeader,
            @RequestParam("target_id_type") String targetIdType,
            @RequestParam("target_id") Long targetId
    );

}
