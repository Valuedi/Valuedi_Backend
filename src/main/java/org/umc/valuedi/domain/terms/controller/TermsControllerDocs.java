package org.umc.valuedi.domain.terms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.umc.valuedi.domain.terms.dto.response.TermsResponseDTO;
import org.umc.valuedi.global.apiPayload.ApiResponse;

@Tag(name = "Terms", description = "약관 조회 API")
public interface TermsControllerDocs {

    @Operation(
            summary = "약관 목록 조회 API",
            description = "비회원 약관 동의 화면에서 표시할 약관 목록 조회",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON200",
                            description = "요청이 성공적으로 처리되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "성공 예시",
                                            summary = "약관 목록 조회 성공 예시",
                                            value = """
                                            {
                                                "isSuccess": true,
                                                "code": "COMMON200",
                                                "message": "요청이 성공적으로 처리되었습니다.",
                                                "result": {
                                                    "termsLists": [
                                                        { "termsId": 1, "code": "AGE_14", "title": "만 14세 이상입니다.", "isRequired": true },
                                                        { "termsId": 2, "code": "SERVICE", "title": "서비스 이용약관 동의", "isRequired": true },
                                                        { "termsId": 3, "code": "PRIVACY", "title": "개인정보 수집 및 이용 동의", "isRequired": true },
                                                        { "termsId": 4, "code": "MARKETING", "title": "마케팅 목적의 개인정보 수집 및 이용 동의", "isRequired": false },
                                                        { "termsId": 5, "code": "SECURITY", "title": "전자금융거래 이용약관 동의", "isRequired": true }
                                                    ]
                                                }
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<ApiResponse<TermsResponseDTO.GetTermsList>> findTermsList();
}
