package org.umc.valuedi.domain.member.converter;

import org.umc.valuedi.domain.member.dto.res.MemberResDTO;
import org.umc.valuedi.domain.member.entity.Member;

public class MemberConverter {

    // Member -> 기본 정보 조회 DTO 변환
    public static MemberResDTO.MemberInfoDTO toMemberInfoDTO(Member member) {
        return MemberResDTO.MemberInfoDTO.builder()
                .memberId(member.getId())
                .name(member.getRealName())
                .build();
    }
}
