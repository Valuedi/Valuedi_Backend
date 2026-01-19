package org.umc.valuedi.domain.auth.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.enums.Status;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryService {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    // 아이디 중복 확인
    public void checkUsernameDuplicate(String username) {
        if(memberRepository.existsByUsernameIncludeDeleted(username)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }
    }

    // 로컬 로그인
    public AuthResDTO.LoginResultDTO loginLocal(AuthReqDTO.LocalLoginDTO dto) {
        Member member = memberRepository.findByUsername(dto.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        if(!passwordEncoder.matches(dto.password(), member.getPasswordHash())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // 휴면 계정이거나 탈퇴한 계정은 로그인 X
        if(member.getStatus() == Status.SUSPENDED) {
            throw new MemberException(MemberErrorCode.MEMBER_SUSPENDED);
        } else if(member.getStatus() == Status.DELETED) {
            throw new MemberException(MemberErrorCode.MEMBER_DELETED);
        }

        CustomUserDetails userDetails = new CustomUserDetails(member);
        String accessToken = jwtUtil.createAccessToken(userDetails);
        String refreshToken = jwtUtil.createRefreshToken(userDetails);

        redisTemplate.opsForValue().set(
                "RT:" + member.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }
}
