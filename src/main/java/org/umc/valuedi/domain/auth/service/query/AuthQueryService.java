package org.umc.valuedi.domain.auth.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.auth.exception.AuthException;
import org.umc.valuedi.domain.auth.exception.code.AuthErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthQueryService {
    private final MemberRepository memberRepository;

    public void checkUsernameDuplicate(String username) {
        if(memberRepository.existsByUsernameIncludeDeleted(username)) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USERNAME);
        }
    }
}
