package org.umc.valuedi.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

import java.io.IOException;

@Tag(name = "Auth", description = "Auth 관련 API")
public interface AuthControllerDocs {

    @Operation(summary = "카카오 로그인 인가 API", description = "카카오 로그인 페이지로 리다이렉트합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 페이지로 이동 성공"
            )
    })
    public void kakaoLogin(HttpServletResponse response) throws IOException;

    @Operation(summary = "카카오 로그인 콜백 API", description = "카카오로부터 인가 코드를 받아 로그인을 완료하고 JWT를 발급합니다.  \n기존에 카카오로 로그인한 적 없는 경우, 회원가입 처리 후 JWT를 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공 예시",
                                    value = """
                        {
                          "isSuccess": true,
                          "code": "AUTH200_1",
                          "message": "로그인에 성공했습니다.",
                          "result": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                            "memberId": 1
                          }
                        }
                        """
                            )
                    )
            )})
    public ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @Parameter(description = "카카오에서 전달한 인가 코드")
            String code
    );
}
