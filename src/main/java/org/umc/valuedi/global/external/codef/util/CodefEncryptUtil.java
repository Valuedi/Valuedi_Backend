package org.umc.valuedi.global.external.codef.util;

import io.codef.api.EasyCodefUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CodefEncryptUtil {

    @Value("${codef.public-key}")
    private String publicKey;

    public String encryptPassword(String plainPassword) {
        try {
            return EasyCodefUtil.encryptRSA(plainPassword, publicKey);
        } catch (Exception e) {
            throw new RuntimeException("비밀번호 암호화 실패: " + e.getMessage(), e);
        }
    }
}