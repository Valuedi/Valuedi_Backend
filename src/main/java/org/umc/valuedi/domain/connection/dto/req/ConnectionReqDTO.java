package org.umc.valuedi.domain.connection.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.umc.valuedi.domain.connection.enums.BusinessType;

public class ConnectionReqDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "금융사 연동 요청")
    public static class Connect {

        @NotBlank
        @Schema(description = "기관 코드 (예: 우리은행 0020)", example = "0020", required = true)
        private String organization;

        @NotBlank
        @Schema(description = "업무 구분 (BK: 은행, CD: 카드)", example = "BK", required = true)
        private String businessType;

        @NotBlank
        @Schema(description = "금융사 로그인 ID", example = "testuser", required = true)
        private String loginId;

        @NotBlank
        @Schema(description = "금융사 로그인 비밀번호", example = "password123", required = true)
        private String loginPassword;

        @Builder.Default
        @Schema(description = "국가 코드", example = "KR")
        private String countryCode = "KR";

        @Builder.Default
        @Schema(description = "고객 구분 (P: 개인, B: 법인)", example = "P")
        private String clientType = "P";

        @Builder.Default
        @Schema(description = "로그인 타입 (1: ID/PW)", example = "1")
        private String loginType = "1";

        @JsonIgnore
        public BusinessType getBusinessTypeEnum() {
            return BusinessType.valueOf(businessType);
        }
    }
}
