package org.umc.valuedi.domain.trophy.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.umc.valuedi.domain.trophy.enums.TrophyType;

@Getter
@Builder
public class TrophyMetaResponse {
    private Long trophyId;
    private String name;
    private TrophyType type;
    private String description;
}
