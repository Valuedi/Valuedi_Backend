package org.umc.valuedi.domain.auth.service.command;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.service.external.KakaoService;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.entity.MemberAuthProvider;
import org.umc.valuedi.domain.member.enums.Provider;
import org.umc.valuedi.domain.member.enums.Status;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberAuthProviderRepository;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.apiPayload.code.GeneralErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

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

        // 휴면 계정이거나 탈퇴한 계정은 로그인 X
        if(member.getStatus() == Status.SUSPENDED) {
            throw new MemberException(MemberErrorCode.MEMBER_SUSPENDED);
        } else if(member.getStatus() == Status.DELETED) {
            throw new MemberException(MemberErrorCode.MEMBER_DELETED);
        }

        CustomUserDetails userDetails = new CustomUserDetails(member);
        String accessToken = jwtUtil.createAccessToken(userDetails);
        String refreshToken = jwtUtil.createRefreshToken(userDetails);

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }

    private Member registerKakao(KakaoResDTO.UserInfoDTO userInfo) {
        try {
            Member newMember = AuthConverter.toMember(userInfo);
            memberRepository.save(newMember);

            MemberAuthProvider authProvider = AuthConverter.toMemberAuthProvider(newMember, String.valueOf(userInfo.getId()));
            memberAuthProviderRepository.save(authProvider);

            return newMember;
        } catch(DataAccessResourceFailureException e) {
            throw new GeneralException(GeneralErrorCode.DB_SERVER_ERROR);
        } catch(DataIntegrityViolationException e) {
            throw new GeneralException(GeneralErrorCode.INVALID_DATA_REQUEST);
        }
    }
}
