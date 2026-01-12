package org.umc.valuedi.domain.auth.service.command;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.service.external.KakaoService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.entity.MemberAuthProvider;
import org.umc.valuedi.domain.member.enums.Gender;
import org.umc.valuedi.domain.member.enums.Provider;
import org.umc.valuedi.domain.member.enums.SignupType;
import org.umc.valuedi.domain.member.enums.Status;
import org.umc.valuedi.domain.member.repository.MemberAuthProviderRepository;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {
    private final JwtUtil jwtUtil;
    private final KakaoService kakaoService;
    private final MemberRepository memberRepository;
    private final MemberAuthProviderRepository memberAuthProviderRepository;

    public AuthResDTO.LoginResultDTO loginKakao(String code) {
        KakaoResDTO.UserInfoDTO userInfo = kakaoService.getKakaoUserInfo(code);
        String providerUserId = String.valueOf(userInfo.getId());

        Member member = memberAuthProviderRepository.findByProviderAndProviderUserId(Provider.KAKAO, providerUserId)
                .map(MemberAuthProvider::getMember)
                .orElseGet(() -> registerKakao(userInfo));

        CustomUserDetails userDetails = new CustomUserDetails(member);
        String accessToken = jwtUtil.createAccessToken(userDetails);
        String refreshToken = jwtUtil.createRefreshToken(userDetails);

        return AuthResDTO.LoginResultDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .build();
    }

    private Member registerKakao(KakaoResDTO.UserInfoDTO userInfo) {
        LocalDate birth = parseToLocalDate(
                userInfo.getKakaoAccount().getBirthyear(),
                userInfo.getKakaoAccount().getBirthday());

        Member newMember = Member.builder()
                .realName(userInfo.getKakaoAccount().getName())
                .birth(birth)
                .gender(Gender.valueOf(userInfo.getKakaoAccount().getGender().toUpperCase()))
                .signupType(SignupType.KAKAO)
                .status(Status.ACTIVE)
                .build();
        memberRepository.save(newMember);

        MemberAuthProvider authProvider = MemberAuthProvider.builder()
                .member(newMember)
                .provider(Provider.KAKAO)
                .providerUserId(String.valueOf(userInfo.getId()))
                .build();
        memberAuthProviderRepository.save(authProvider);

        return newMember;
    }

    private LocalDate parseToLocalDate(String year, String day) {
        if(year == null || day == null) return null;
        return LocalDate.parse(year + day, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
