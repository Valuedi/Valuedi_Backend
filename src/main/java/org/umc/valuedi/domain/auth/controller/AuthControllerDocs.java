package org.umc.valuedi.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Auth", description = "Auth 관련 API (로그인, 회원가입 등)")
public interface AuthControllerDocs {

    @Operation(
            summary = "카카오 로그인 URL 생성 API",
            description = "카카오 로그인 페이지로 이동하기 위한 URL을 생성하고, 보안을 위한 state 값을 쿠키에 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 카카오 로그인 URL 생성",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "생성된 URL에는 보안을 위한 state 파라미터가 추가되어 있습니다.",
                                    value = """
                        {
                          "isSuccess": true,
                          "code": "AUTH200_1",
                          "message": "카카오 로그인 URL이 성공적으로 생성되었습니다.",
                          "result": "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id={clientId}&redirect_uri={redirectUri}&state=bba27165-..."
                        }
                        """
                            )
                    )
            )
    })
    ApiResponse<String> kakaoLogin(HttpServletResponse response);

    @Operation(
            summary = "카카오 로그인 콜백 API",
            description = "카카오로부터 인가 코드를 받아 로그인을 완료하고 JWT를 발급합니다.  \n기존에 카카오로 로그인한 적 없는 경우, 회원가입 처리 후 JWT를 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 로그인 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_2",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "에러 - 보안 인증 실패",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "보안 인증 실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_5",
                                              "message": "보안 인증 값(state)이 일치하지 않거나 만료되었습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @Parameter(description = "카카오에서 전달한 인가 코드")
            String code,
            @Parameter(description = "카카오 로그인 URL 생성 시 서버에서 생성한 state 값")
            String state,
            @Parameter(description = "서버에서 보낸 쿠키에 저장된 state 값")
            String oauthState,
            HttpServletResponse response
    );

    @Operation(summary = "아이디 중복 확인 API", description = "사용자가 입력한 아이디의 중복 여부를 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 사용 가능한 아이디",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_3",
                                              "message": "사용 가능한 아이디입니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "에러 - 이미 사용 중인 아이디",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "중복 에러 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH409_1",
                                              "message": "이미 사용 중인 아이디입니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "에러 - 유효성 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "유효성 검증 실패 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "VALID400_1",
                                              "message": "검증에 실패했습니다.",
                                              "result": {
                                                "username": "아이디를 입력해주세요."
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    ApiResponse<Void> checkUsername(
            @Parameter(description = "중복 확인할 아이디", example = "valuedi_123")
            @NotBlank(message = "아이디를 입력해주세요.")
            String username
    );

    @Operation(summary = "이메일 인증번호 발송 API", description = "회원가입 시 사용자가 입력한 이메일로 인증번호를 발송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 인증번호 발송",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "발송 성공 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH200_4",
                                                      "message": "인증번호가 발송되었습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "에러 - 메일 발송 실패",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "발송 실패 예시",
                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH503_1",
                                                      "message": "메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> sendEmail(@Valid AuthReqDTO.EmailSendDTO dto);

    @Operation(summary = "이메일 인증번호 검증 API", description = "회원가입 시 사용자 이메일로 발송한 인증번호를 검증합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 이메일 인증 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 인증 성공 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH200_5",
                                                      "message": "이메일 인증에 성공했습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "에러 - 인증번호 만료 또는 존재하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "인증번호 만료 예시",
                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH404_1",
                                                      "message": "인증번호가 만료되었거나 존재하지 않습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "에러 - 인증번호 불일치",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "인증번호 불일치 예시",
                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH400_2",
                                                      "message": "인증번호가 일치하지 않습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            )
    })
    ApiResponse<Void> verifyEmail(@Valid AuthReqDTO.EmailVerifyDTO dto);
}
