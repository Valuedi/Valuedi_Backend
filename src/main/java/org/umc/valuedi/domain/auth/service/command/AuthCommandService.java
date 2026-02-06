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
import org.umc.valuedi.domain.terms.service.MemberTermsService;
import org.umc.valuedi.global.apiPayload.code.GeneralErrorCode;
import org.umc.valuedi.global.apiPayload.exception.GeneralException;
import org.umc.valuedi.global.security.jwt.JwtUtil;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
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
    private final MemberTermsService memberTermsService;

    private static final SecureRandom sr = new SecureRandom();
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BL:";

    // 카카오 로그인
    public AuthResDTO.LoginResultDTO loginKakao(String code) {
        KakaoResDTO.UserTokenInfo userTokenInfo = kakaoService.getKakaoUserInfo(code);
        String providerUserId = String.valueOf(userTokenInfo.userInfo().getId());

        Member member = memberAuthProviderRepository.findByProviderAndProviderUserId(Provider.KAKAO, providerUserId)
                .map(MemberAuthProvider::getMember)
                .orElseGet(() -> registerKakao(userTokenInfo));

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
                REFRESH_TOKEN_PREFIX + member.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }

    // 카카오로 회원가입
    private Member registerKakao(KakaoResDTO.UserTokenInfo userTokenInfo) {
        try {
            // 1. AuthConverter를 통해 엔티티로 변환
            Member newMember = AuthConverter.toKakaoMember(userTokenInfo.userInfo());

            // 2. username이 누락된 경우(소셜 가입) 고유 식별값 생성
            if (newMember.getUsername() == null || newMember.getUsername().isBlank()) {
                newMember.setUsername(generateUniqueUsername(newMember.getEmail()));
            }

            // 3. Member 저장
            Member savedMember = memberRepository.save(newMember);

            // 약관 정보 저장
            memberTermsService.saveTermsForRegistration(
                    savedMember,
                    kakaoService.getKakaoServiceTerms(userTokenInfo.accessToken()));

            // 제공자 정보 저장
            MemberAuthProvider authProvider = AuthConverter.toMemberAuthProvider(savedMember, String.valueOf(userTokenInfo.userInfo().getId()));
            memberAuthProviderRepository.save(authProvider);

            return savedMember;
        } catch(DataAccessResourceFailureException e) {
            throw new GeneralException(GeneralErrorCode.DB_SERVER_ERROR);
        } catch(DataIntegrityViolationException e) {
            throw new GeneralException(GeneralErrorCode.INVALID_DATA_REQUEST);
        }
    }

    // username 생성 로직 (이메일 + 랜덤 숫자)
    private String generateUniqueUsername(String email) {
        String prefix = (email != null && email.contains("@"))
                ? email.split("@")[0]
                : "user";

        // 초기 후보 생성
        String candidate = prefix + "_" + (sr.nextInt(9000) + 1000);

        // 최대 5번까지 DB 중복 체크 후 재시도
        int attempts = 0;
        while (memberRepository.existsByUsername(candidate) && attempts < 5) {
            candidate = prefix + "_" + (sr.nextInt(9000) + 1000);
            attempts++;
        }

        if (attempts >= 5) {
            candidate = prefix + "_" + (System.currentTimeMillis() % 10000);
        }

        return candidate;
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

            if (!"true".equals(isVerified)) {
                throw new AuthException(AuthErrorCode.EMAIL_NOT_VERIFIED);
            }

            authQueryService.checkUsernameDuplicate(dto.username());

            String encodedPassword = passwordEncoder.encode(dto.password());
            Member newMember = AuthConverter.toGeneralMember(dto, encodedPassword);

            Member savedMember = memberRepository.save(newMember);
            memberTermsService.saveTermsForRegistration(savedMember, dto.agreements());

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

        if(member.getStatus() == Status.SUSPENDED) {
            throw new MemberException(MemberErrorCode.MEMBER_SUSPENDED);
        } else if(member.getStatus() == Status.DELETED) {
            throw new MemberException(MemberErrorCode.MEMBER_DELETED);
        }

        CustomUserDetails userDetails = new CustomUserDetails(member);
        String accessToken = jwtUtil.createAccessToken(userDetails);
        String refreshToken = jwtUtil.createRefreshToken(userDetails);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + member.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }

    // 토큰 재발급
    public AuthResDTO.LoginResultDTO tokenReissue(String accessToken, String refreshToken) {
        try {
            if(!jwtUtil.getCategory(refreshToken).equals("refresh")) {
                throw new AuthException(AuthErrorCode.NOT_REFRESH_TOKEN);
            }
        } catch(ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        if(accessToken != null && accessToken.startsWith(BEARER_PREFIX)) {
            accessToken = accessToken.substring(BEARER_PREFIX.length());
            long expiration = jwtUtil.getExpiration(accessToken);
            long diff = expiration - System.currentTimeMillis();

            if(diff > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + accessToken,
                        "reissue_waste",
                        diff,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        Long memberId = Long.valueOf(jwtUtil.getMemberId(refreshToken));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String redisKey = REFRESH_TOKEN_PREFIX + member.getId();
        String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

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
    public void logout(Long memberId, String accessToken) {
        if(accessToken == null || !accessToken.startsWith(BEARER_PREFIX)){
            throw new AuthException(AuthErrorCode.INVALID_TOKEN_FORMAT);
        }
        String resolveToken = accessToken.substring(BEARER_PREFIX.length());

        String redisKey = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(redisKey);

        long expiration = jwtUtil.getExpiration(resolveToken);
        long diff = expiration - System.currentTimeMillis();

        if(diff > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + resolveToken,
                    "logout",
                    diff,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}