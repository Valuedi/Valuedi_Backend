package org.umc.valuedi.domain.auth.converter;

import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.entity.MemberAuthProvider;
import org.umc.valuedi.domain.member.enums.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AuthConverter {

    // 카카오 유저 정보 바탕으로 Member 엔티티 생성
    public static Member toKakaoMember(KakaoResDTO.UserInfoDTO userInfo) {
        KakaoResDTO.UserInfoDTO.KakaoAccount account = userInfo.getKakaoAccount();

        // 필수 필드(이름, 생일, 성별)가 없는 경우 예외 발생
        validateRequiredFields(account);

        return Member.builder()
                .realName(account.getName())
                .birth(parseToLocalDate(account.getBirthyear(), account.getBirthday()))
                .gender(Gender.valueOf(account.getGender().toUpperCase()))
                .signupType(SignupType.KAKAO)
                .status(Status.ACTIVE)
                .build();
    }

    // 카카오로 회원가입 시 MemberAuthProvider 엔티티 생성
    public static MemberAuthProvider toMemberAuthProvider(Member member, String providerUserId) {
        return MemberAuthProvider.builder()
                .member(member)
                .provider(Provider.KAKAO)
                .providerUserId(providerUserId)
                .build();
    }

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

    // 카카오에서 넘어온 정보에서 필수 필드가 모두 있는지 검증
    private static void validateRequiredFields(KakaoResDTO.UserInfoDTO.KakaoAccount account) {
        if (account.getName() == null || account.getGender() == null ||
                account.getBirthyear() == null || account.getBirthday() == null) {

            throw new AuthException(AuthErrorCode.REQUIRED_INFO_MISSING);
        }
    }

    // 카카오에서 넘어온 생일 정보를 LocalDate로 변환
    private static LocalDate parseToLocalDate(String year, String day) {
        return LocalDate.parse(year + day, DateTimeFormatter.ofPattern("yyyyMMdd"));
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
