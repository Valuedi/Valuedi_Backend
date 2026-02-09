package org.umc.valuedi.global.external.kakao.converter;

import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.entity.MemberAuthProvider;
import org.umc.valuedi.domain.member.enums.Gender;
import org.umc.valuedi.domain.member.enums.Provider;
import org.umc.valuedi.domain.member.enums.SignupType;
import org.umc.valuedi.domain.member.enums.Status;
import org.umc.valuedi.global.external.kakao.dto.res.KakaoResDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KakaoConverter {

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
}
