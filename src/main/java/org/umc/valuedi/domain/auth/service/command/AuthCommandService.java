package org.umc.valuedi.domain.auth.service.command;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.event.AuthMailEvent;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.dto.req.AuthReqDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.auth.service.external.KakaoService;
import org.umc.valuedi.domain.auth.service.query.AuthQueryService;
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

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {
    private final JwtUtil jwtUtil;
    private final KakaoService kakaoService;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final AuthQueryService authQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final MemberAuthProviderRepository memberAuthProviderRepository;
    private static final SecureRandom sr = new SecureRandom();

    // 카카오 로그인
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

        redisTemplate.opsForValue().set(
                "RT:" + member.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }

    // 카카오로 회원가입
    private Member registerKakao(KakaoResDTO.UserInfoDTO userInfo) {
        try {
            Member newMember = AuthConverter.toKakaoMember(userInfo);
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

    // 이메일 인증번호 발송
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendCode(String email) {
        String cooldownKey = "MAIL_COOLDOWN:" + email;
        String redisKey = "AUTH_CODE:" + email;

        Boolean isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "true", Duration.ofSeconds(60));

        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_SENT);
        }

        String code = String.valueOf(sr.nextInt(900_000) + 100_000);
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(3));
        eventPublisher.publishEvent(new AuthMailEvent(email, code));
    }

    // 이메일 인증번호 검증
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void verifyCode(String email, String code) {
        String redisKey = "AUTH_CODE:" + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if(savedCode == null) {
            throw new AuthException(AuthErrorCode.EMAIL_CODE_NOT_FOUND);
        }

        if(!savedCode.equals(code)) {
            throw new AuthException(AuthErrorCode.EMAIL_CODE_MISMATCH);
        }

        redisTemplate.opsForValue().set("EMAIL_VERIFIED:" + email, "true", Duration.ofMinutes(5));
        redisTemplate.delete(redisKey);
    }

    // 로컬 회원가입
    public AuthResDTO.RegisterResDTO registerLocal(AuthReqDTO.RegisterReqDTO dto) {
        try {
            String email = dto.email();
            String verifiedKey = "EMAIL_VERIFIED:" + email;
            String isVerified = redisTemplate.opsForValue().get(verifiedKey);

            // 이메일 인증을 안 했을 경우 예외 발생
            if (!"true".equals(isVerified)) {
                throw new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED);
            }

            // DB 저장 전 한번 더 아이디 중복 확인
            authQueryService.checkUsernameDuplicate(dto.username());

            String encodedPassword = passwordEncoder.encode(dto.password());
            Member newMember = AuthConverter.toGeneralMember(dto, encodedPassword);

            Member savedMember = memberRepository.save(newMember);

            return AuthConverter.toRegisterResDTO(savedMember);
        } catch (DataAccessResourceFailureException e) {
            throw new GeneralException(GeneralErrorCode.DB_SERVER_ERROR);
        } catch(DataIntegrityViolationException e) {
            throw new GeneralException(GeneralErrorCode.INVALID_DATA_REQUEST);
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

    // 엑세스/리프레시 토큰 재발급
    public AuthResDTO.LoginResultDTO tokenReissue(String accessToken, String refreshToken) {
        // 리프레시 토큰이 맞는지 확인. 아닐 경우와 토큰 자체가 유효하지 않을 경우 예외 발생
        try {
            if(!jwtUtil.getCategory(refreshToken).equals("refresh")) {
                throw new AuthException(AuthErrorCode.NOT_REFRESH_TOKEN);
            }
        } catch(ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // 기존 엑세스 토큰이 만료되지 않았다면 무효화
        if(accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.replace("Bearer ", "");

            long expiration = jwtUtil.getExpiration(accessToken);
            long diff = expiration - System.currentTimeMillis();

            // 0보다 작거나 같으면 이미 만료된 토큰
            if(diff > 0) {
                redisTemplate.opsForValue().set(
                        "BL:" + accessToken,
                        "reissue_waste",
                        diff,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        Long memberId = Long.valueOf(jwtUtil.getMemberId(refreshToken));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String redisKey = "RT:" + member.getId();
        String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        // Redis에 저장된 토큰인지 확인
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        CustomUserDetails userDetails = new CustomUserDetails(member);
        String newAccessToken = jwtUtil.createAccessToken(userDetails);
        String newRefreshToken = jwtUtil.createRefreshToken(userDetails);

        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthConverter.toLoginResultDTO(member, newAccessToken, newRefreshToken);
    }

    // 로그아웃
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void logout(String accessToken) {
        String resolveToken = accessToken.substring(7);
        Long memberId = Long.valueOf(jwtUtil.getMemberId(resolveToken));

        String redisKey = "RT:" + memberId;
        redisTemplate.delete(redisKey);

        long expiration = jwtUtil.getExpiration(resolveToken);
        long diff = expiration - System.currentTimeMillis();

        // 0보다 작거나 같으면 이미 만료된 토큰
        if(diff > 0) {
            redisTemplate.opsForValue().set(
                    "BL:" + resolveToken,
                    "logout",
                    diff,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}
