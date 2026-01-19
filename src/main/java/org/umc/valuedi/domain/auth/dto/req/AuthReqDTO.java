package org.umc.valuedi.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    public record RegisterReqDTO(
            @NotBlank(message = "아이디를 입력해주세요.")
            String username,

            @NotBlank(message = "이름을 입력해주세요.")
            String realName,

            @NotBlank(message = "주민등록번호 앞 7자리는 필수 입력 항목입니다.")
            @Pattern(regexp = "\\d{7}", message = "주민등록번호 앞자리는 숫자 7자리로 입력해주세요.")
            String rrnPrefix,

            @NotBlank(message = "비밀번호를 입력해주세요.")
            String password,

            @NotBlank(message = "이메일을 입력해주세요.")
            String email
    ) {}
}
