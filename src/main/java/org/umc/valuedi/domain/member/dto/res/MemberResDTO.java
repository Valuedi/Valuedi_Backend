package org.umc.valuedi.domain.member.dto.res;

import lombok.Builder;

public class MemberResDTO {

    @Builder
    public record MemberInfoDTO(
            Long memberId,
            String name
    ) {}
}
