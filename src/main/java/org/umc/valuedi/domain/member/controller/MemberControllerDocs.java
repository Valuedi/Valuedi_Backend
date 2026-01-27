package org.umc.valuedi.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.umc.valuedi.domain.member.dto.res.MemberResDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Member", description = "Member 관련 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "기본 정보 조회(이름) API",
            description = "로그인한 사용자의 기본 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 기본 정보 조회",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "기본 정보 조회 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEMBER200_1",
                                              "message": "회원 기본 정보 조회에 성공했습니다.",
                                              "result": {
                                                "memberId": 9,
                                                "name": "밸류디"
                                              }
                                            }
                                    """
                            )
                    )
            )
    })
    public ApiResponse<MemberResDTO.MemberInfoDTO> getMemberInfo(
            Long memberId
    );
}
