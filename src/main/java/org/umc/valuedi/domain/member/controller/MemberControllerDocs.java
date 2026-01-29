package org.umc.valuedi.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.umc.valuedi.domain.member.dto.req.MemberReqDTO;
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
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "회원 탈퇴 API",
            description = "카카오/로컬 계정으로 가입한 회원을 탈퇴시킵니다.  \n탈퇴 사유는 다음과 같이 요청해야 합니다.  \nNOT_HELPFUL - 금융 관리에 도움이 되지 않았어요  \nDIFFICULT_TO_USE - 사용이 어려워요  \nMISSING_FEATURES - 필요한 기능이 없어요  \nSECURITY_CONCERNS - 보안이 걱정돼요  \nFREQUENT_ERRORS - 오류가 자주 발생해요  \nOTHER - 기타")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 회원 탈퇴 완료",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "회원 탈퇴 예시",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEMBER200_2",
                                              "message": "회원 탈퇴가 완료되었습니다.",
                                              "result": null
                                            }
                                    """
                            )
                    )
            )
    })
    public ApiResponse<Void> deleteMember(
            @Parameter(hidden = true) Long memberId,
            @Parameter(hidden = true) String accessToken,
            @Valid MemberReqDTO.MemberWithdrawDTO memberWithdrawDTO
    );
}
