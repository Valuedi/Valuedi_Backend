package org.umc.valuedi.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthReqDTO {

    public record EmailSendDTO (
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 이메일", example = "valuedi@example.com")
        String email
    ) {}

    public record EmailVerifyDTO (
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 이메일", example = "valuedi@example.com")
        String email,

        @NotBlank(message = "인증코드를 입력해주세요.")
        @Schema(description = "인증번호 6자리", example = "123456")
        String code
    ) {}
}
