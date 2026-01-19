package org.umc.valuedi.global.external.codef.util;

import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.external.codef.config.CodefProperties;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;
import org.umc.valuedi.global.external.codef.exception.CodefException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodefEncryptUtil {

    private final CodefProperties codefProperties;

    /**
     * 은행/카드사 비밀번호 RSA 암호화
     */
    public String encrypt(String plainText) {
        try {
            return EasyCodefUtil.encryptRSA(plainText, codefProperties.getPublicKey());
        } catch (Exception e) {
            log.error("CODEF 암호화 실패: {}", e.getMessage());
            throw new CodefException(CodefErrorCode.CODEF_ENCRYPTION_ERROR);
        }
    }
}
