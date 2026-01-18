package org.umc.valuedi.domain.auth.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.kakao.KakaoResDTO;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
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

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {
    private final JwtUtil jwtUtil;
    private final KakaoService kakaoService;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
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

        return AuthConverter.toLoginResultDTO(member, accessToken, refreshToken);
    }

    // 카카오로 회원가입
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

    // 이메일 인증번호 발송
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendCode(String email) {
        String code = String.valueOf(sr.nextInt(900_000) + 100_000);
        String redisKey = "AUTH_CODE:" + email;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(3));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[Valuedi] 회원가입 인증번호입니다.");
            message.setText("인증번호는 [" + code + "] 입니다. 3분 이내에 입력해 주세요.");
            mailSender.send(message);
        } catch (MailException e) {
            redisTemplate.delete(redisKey);
            throw new AuthException(AuthErrorCode.MAIL_SEND_ERROR);
        }
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
}
