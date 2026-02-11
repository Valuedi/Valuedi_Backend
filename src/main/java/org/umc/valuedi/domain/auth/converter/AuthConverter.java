package org.umc.valuedi.domain.auth.converter;

import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.enums.Gender;
import org.umc.valuedi.domain.member.enums.Role;
import org.umc.valuedi.domain.member.enums.SignupType;
import org.umc.valuedi.domain.member.enums.Status;

import java.time.LocalDate;

public class AuthConverter {

    // 로컬 회원가입 정보 바탕으로 Member 엔티티 생성
    public static Member toGeneralMember(AuthReqDTO.RegisterReqDTO dto, String encodedPassword) {
        return Member.builder()
                .username(dto.username())
                .email(dto.email())
                .realName(dto.realName())
                .birth(extractBirth(dto.rrnPrefix()))
                .gender(extractGender(dto.rrnPrefix()))
                .role(Role.ROLE_USER)
                .passwordHash(encodedPassword)
                .signupType(SignupType.LOCAL)
                .status(Status.ACTIVE)
                .build();
    }

    // 회원가입 완료 응답 DTO로 변환
    public static AuthResDTO.RegisterResDTO toRegisterResDTO(Member member) {
        return AuthResDTO.RegisterResDTO.builder()
                .memberId(member.getId())
                .build();
    }

    // 최종 로그인 응답 DTO로 변환
    public static AuthResDTO.LoginResultDTO toLoginResultDTO(Member member, String accessToken, String refreshToken) {
        return AuthResDTO.LoginResultDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .build();
    }

    // 로그인 상태를 DTO로 변환
    public static AuthResDTO.AuthStatusDTO toAuthStatusDTO(Boolean authStatus, Long memberId) {
        return AuthResDTO.AuthStatusDTO.builder()
                .isLogin(authStatus)
                .memberId(memberId)
                .build();
    }

    // 주민등록번호로 생일 변환
    private static LocalDate extractBirth(String rrnPrefix) {
        int yearPrefix = Integer.parseInt(rrnPrefix.substring(0, 2));
        char genderDigit = rrnPrefix.charAt(6);
        int fullYear = (genderDigit == '1' || genderDigit == '2') ? 1900 + yearPrefix : 2000 + yearPrefix;

        return LocalDate.of(fullYear,
                Integer.parseInt(rrnPrefix.substring(2, 4)),
                Integer.parseInt(rrnPrefix.substring(4, 6)));
    }

    // 주민등록번호로 성별 변환
    private static Gender extractGender(String rrnPrefix) {
        char genderDigit = rrnPrefix.charAt(6);
        return (genderDigit == '1' || genderDigit == '3') ? Gender.MALE : Gender.FEMALE;
    }
}
