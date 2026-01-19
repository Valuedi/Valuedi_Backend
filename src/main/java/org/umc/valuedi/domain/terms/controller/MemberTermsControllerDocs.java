package org.umc.valuedi.domain.terms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Terms", description = "약관 조회 API")
public interface MemberTermsControllerDocs {

    @Operation(
            summary = "사용자가 동의한 약관 조회 API",
            description = "사용자ID로 사용자가 동의한 약관 목록(약관ID, 동의버전, 동의일시)을 조회합니다. 동의일시는 재동의가 발생하면 최신으로 갱신됩니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "성공 예시",
                                            summary = "사용자가 동의한 약관 조회 성공 예시",
                                            value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON200",
                                                "message": "요청이 성공적으로 처리되었습니다.",
                                                "result": {
                                                    "agreements": [
                                                        {
                                                            "termsId": 1,
                                                            "agreedVersion": "1.0",
                                                            "agreedAt": "2026-01-18T23:30:00"
                                                        },
                                                        {
                                                            "termsId": 2,
                                                            "agreedVersion": "1.0",
                                                            "agreedAt": "2026-01-18T23:30:00"
                                                        },
                                                        {
                                                            "termsId": 3,
                                                            "agreedVersion": "1.0",
                                                            "agreedAt": "2026-01-18T23:30:00"
                                                        }
                                                    ]
                                                }
                                            }
                                            """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "MEMBER404_1",
                            description = "해당 회원을 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "회원 없음 예시",
                                            summary = "사용자 약관 동의 조회 실패(회원 없음) 예시",
                                            value = """
                                            {
                                                "isSuccess": false,
                                                "code": "MEMBER404_1",
                                                "message": "해당 회원을 찾을 수 없습니다.",
                                                "result": null,
                                                "timestamp": "2026-01-18T23:30:00"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<ApiResponse<TermsResponseDTO.GetMemberAgreements>> findMemberAgreements(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam("memberId") Long memberId
    );
}

