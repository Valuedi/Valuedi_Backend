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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;
import org.umc.valuedi.global.security.annotation.CurrentMember;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

@Tag(name = "Auth", description = "Auth 관련 API (로그인, 회원가입 등)")
public interface AuthControllerDocs {

    @Operation(
            summary = "카카오 로그인 URL 생성 API",
            description = "카카오 로그인 페이지로 이동하기 위한 URL을 생성하고, 보안을 위한 state 값을 함께 응답합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 카카오 로그인 URL 생성",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "카카오 로그인 URL 생성 예시",
                                    value = """
                        {
                          "isSuccess": true,
                          "code": "AUTH200_1",
                          "message": "카카오 로그인 URL이 성공적으로 생성되었습니다.",
                          "result": {
                            "url": "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id={clientId}&redirect_uri={redirectUri}&state=6af5e726-...",
                            "state": "6af5e726-7df6-4d03-9829-4b7d4ad792ec"
                          }
                        }
                        """
                            )
                    )
            )
    })
    ApiResponse<AuthResDTO.LoginUrlDTO> kakaoLogin();

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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "에러 - 로그인 실패(휴면/탈퇴 회원)",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "휴면회원 로그인 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER403_1",
                                              "message": "휴면 상태의 회원입니다.",
                                              "result": null
                                            }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "탈퇴회원 로그인 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER403_2",
                                              "message": "탈퇴한 회원입니다.",
                                              "result": null
                                            }
                                    """
                                    )
                            }
                    )
            )
    })
    ApiResponse<AuthResDTO.LoginResultDTO> kakaoCallback(
            @Parameter(description = "카카오에서 전달한 인가 코드")
            String code,
            @Parameter(description = "카카오에서 전달한 state 값")
            String state,
            @Parameter(description = "클라이언트가 저장해둔 원본 state 값")
            String originalState
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
                    description = "성공 - 인증번호 발송 요청",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "발송 요청 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH200_4",
                                                      "message": "인증번호 발송 요청이 접수되었습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "에러 - 인증번호 발송 요청 과다",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "과다 요청 예시",
                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH429_1",
                                                      "message": "이미 인증번호가 발송되었습니다. 1분 후 다시 시도해 주세요.",
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

    @Operation(summary = "로컬 계정 회원가입 API", description = "회원가입을 통해 새로운 로컬 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "성공 - 회원가입 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH201_1",
                                                      "message": "회원가입이 성공적으로 완료되었습니다.",
                                                      "result": {
                                                        "memberId": 5
                                                       }
                                                    }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "에러 - 이메일 인증 미실시",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "이메일 인증 미실시 예시",
                                    value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "AUTH403_1",
                                                      "message": "이메일 인증이 완료되지 않았습니다.",
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
                                    name = "아이디 중복 에러 예시",
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
            )
    })
    public ResponseEntity<ApiResponse<AuthResDTO.RegisterResDTO>> signUp(
            @Valid AuthReqDTO.RegisterReqDTO dto
    );

    @Operation(
            summary = "로컬 계정 로그인 API",
            description = "로컬 계정으로 로그인을 시도합니다. 로그인이 완료되면 JWT를 발급합니다.")
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
                    description = "에러 - 로그인 실패(계정정보 불일치)",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "계정정보 불일치 예시",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "AUTH401_6",
                                              "message": "아이디 또는 비밀번호가 일치하지 않습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "에러 - 로그인 실패(휴면/탈퇴 회원)",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "휴면회원 로그인 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER403_1",
                                              "message": "휴면 상태의 회원입니다.",
                                              "result": null
                                            }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "탈퇴회원 로그인 예시",
                                            value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MEMBER403_2",
                                              "message": "탈퇴한 회원입니다.",
                                              "result": null
                                            }
                                    """
                                    )
                            }
                    )
            )
    })
    public ApiResponse<AuthResDTO.LoginResultDTO> localLogin(
            @Valid AuthReqDTO.LocalLoginDTO dto
    );

    @Operation(
            summary = "토큰 재발급 API",
            description = "리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.  \n요청 헤더에 만료되지 않은 액세스 토큰이 있다면 이를 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 토큰 재발급 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "토큰 재발급 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH200_6",
                                                      "message": "토큰 재발급에 성공했습니다.",
                                                      "result": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                        "memberId": 1
                                                      }
                                                    }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<AuthResDTO.LoginResultDTO> tokenReissue(
            @Parameter(hidden = true)
            String accessToken,
            String refreshToken
    );

    @Operation(
            summary = "로그아웃 API",
            description = "카카오/로컬로 로그인한 계정을 로그아웃 시킵니다.  \n요청 시 헤더에 포함된 액세스 토큰을 이용해 액세스/리프레시 토큰을 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 로그아웃 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "로그아웃 예시",
                                    value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "AUTH200_7",
                                                      "message": "로그아웃이 완료되었습니다.",
                                                      "result": null
                                                    }
                                            """
                            )
                    )
            )
    })
    public ApiResponse<Void> logout(
            Long memberId,
            @Parameter(hidden = true)
            String accessToken
    );

    @Operation(
            summary = "로그인 상태 조회 API",
            description = "현재 접속 중인 사용자의 로그인 상태를 조회합니다.  \n요청 헤더의 액세스 토큰이 유효하지 않거나 없는 경우 비로그인 상태이며, 액세스 토큰이 유효할 경우 로그인 상태입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 로그인 상태 조회(로그인/비로그인)",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "로그인 상태 예시",
                                            value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_8",
                                              "message": "로그인 상태 조회에 성공했습니다.",
                                              "result": {
                                                "isLogin": true,
                                                "memberId": 9
                                              }
                                            }
                                    """
                                    ),
                                    @ExampleObject(
                                            name = "비로그인 상태 예시",
                                            value = """
                                            {
                                              "isSuccess": true,
                                              "code": "AUTH200_8",
                                              "message": "로그인 상태 조회에 성공했습니다.",
                                              "result": {
                                                "isLogin": false,
                                                "memberId": null
                                              }
                                            }
                                    """
                                    )
                            }
                    )
            )
    })
    public ApiResponse<AuthResDTO.AuthStatusDTO> getAuthStatus(
            @Parameter(hidden = true) Long memberId
    );
}
