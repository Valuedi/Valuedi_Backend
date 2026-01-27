package org.umc.valuedi.global.external.codef.util;

import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.external.codef.config.CodefProperties;
import org.umc.valuedi.global.external.codef.exception.code.CodefErrorCode;
import org.umc.valuedi.global.external.codef.exception.CodefException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptUtil {

    private final CodefProperties codefProperties;
    private static final String ALGORITHM = "AES";

    /**
     * 은행/카드사 비밀번호 RSA 암호화
     */
    public String encryptRSA(String plainText) {
        if (plainText == null) return null;
        try {
            return EasyCodefUtil.encryptRSA(plainText, codefProperties.getPublicKey());
        } catch (Exception e) {
            log.error("CODEF RSA 암호화 실패: {}", e.getMessage());
            throw new CodefException(CodefErrorCode.CODEF_ENCRYPTION_ERROR);
        }
    }

    /**
     * 계좌번호 AES 암호화
     */
    public byte[] encryptAES(String value) {
        if (value == null) return null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(codefProperties.getAesSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("CODEF AES 암호화 실패: {}", e.getMessage(), e);
            throw new CodefException(CodefErrorCode.CODEF_ENCRYPTION_ERROR);
        }
    }

    /**
     * 계좌번호 AES 복호화
     */
    public String decryptAES(byte[] encryptedValue) {
        if (encryptedValue == null) return null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(codefProperties.getAesSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(encryptedValue), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("CODEF AES 복호화 실패: {}", e.getMessage(), e);
            throw new CodefException(CodefErrorCode.CODEF_DECRYPTION_ERROR);
        }
    }
}
