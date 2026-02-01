package org.umc.valuedi.domain.auth.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.auth.converter.AuthConverter;
import org.umc.valuedi.domain.auth.dto.res.AuthResDTO;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryService {
    private final MemberRepository memberRepository;

    // 아이디 중복 확인
    public void checkUsernameDuplicate(String username) {
        if(memberRepository.existsByUsernameIncludeDeleted(username)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }
    }

    // 로그인 상태 조회
    public AuthResDTO.AuthStatusDTO getAuthStatus(Long memberId) {
        if(memberId == null) {
            return AuthConverter.toAuthStatusDTO(false, null);
        }

        return AuthConverter.toAuthStatusDTO(true, memberId);
    }
}
