package org.umc.valuedi.domain.member.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.enums.WithdrawalReason;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberAuthProviderRepository;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.global.security.jwt.JwtUtil;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {
    private final MemberAuthProviderRepository memberAuthProviderRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public void withdraw(Long memberId, String accessToken, WithdrawalReason withdrawalReason) {
        String resolveToken = accessToken.substring(7);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        memberAuthProviderRepository.findByMemberId(memberId).ifPresent(provider -> {
            provider.anonymize();
            memberAuthProviderRepository.saveAndFlush(provider);
            memberAuthProviderRepository.delete(provider);
        });

        member.withdraw(withdrawalReason);
        memberRepository.saveAndFlush(member);
        memberRepository.delete(member);

        redisTemplate.delete("RT:" + memberId);

        long expiration = jwtUtil.getExpiration(resolveToken);
        long diff = expiration - System.currentTimeMillis();

        // 0보다 작거나 같으면 이미 만료된 토큰
        if(diff > 0) {
            redisTemplate.opsForValue().set(
                    "BL:" + resolveToken,
                    "withdraw",
                    diff,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}
