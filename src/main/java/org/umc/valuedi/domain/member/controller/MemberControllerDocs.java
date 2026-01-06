package org.umc.valuedi.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Member", description = "Member 관련 API (로그인, 회원가입 등)")
public interface MemberControllerDocs {

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
                      "code": "MEMBER200_1",
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
                      "code": "MEMBER409_1",
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
}
