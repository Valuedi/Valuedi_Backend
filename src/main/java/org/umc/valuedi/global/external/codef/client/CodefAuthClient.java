package org.umc.valuedi.global.external.codef.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.valuedi.global.external.codef.dto.res.CodefTokenResDTO;

@FeignClient(name = "CodefAuthClient", url = "https://oauth.codef.io")
public interface CodefAuthClient {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CodefTokenResDTO getAccessToken(
            @RequestHeader("Authorization") String basicAuth,
            @RequestParam("grant_type") String grantType,
            @RequestParam("scope") String scope
    );
}
