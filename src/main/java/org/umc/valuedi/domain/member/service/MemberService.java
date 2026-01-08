package org.umc.valuedi.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.exception.MemberException;
import org.umc.valuedi.domain.member.exception.code.MemberErrorCode;
import org.umc.valuedi.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public void checkUsernameDuplicate(String username) {
        Integer exists = memberRepository.existsByUsernameIncludeDeleted(username);

        if(exists != null && exists == 1) {
            throw new MemberException(MemberErrorCode.DUPLICATE_USERNAME);
        }
    }

}
