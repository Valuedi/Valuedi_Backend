package org.umc.valuedi.domain.member.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberRepository memberRepository;

    public void checkUsernameDuplicate(String username) {
        if(memberRepository.existsByUsernameIncludeDeleted(username)) {
            throw new MemberException(MemberErrorCode.DUPLICATE_USERNAME);
        }
    }
}
