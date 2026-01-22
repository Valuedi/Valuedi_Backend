package org.umc.valuedi.domain.trophy.converter;


import org.umc.valuedi.domain.trophy.dto.response.TrophyMetaResponse;
import org.umc.valuedi.domain.trophy.dto.response.TrophyResponse;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;

import java.util.List;
import java.util.stream.Collectors;

public class TrophyConverter {

    // 단일 스냅샷 -> 응답 DTO 변환
    public static TrophyResponse toTrophyResponse(MemberTrophySnapshot snapshot) {
        Trophy trophy = snapshot.getTrophy();
        return TrophyResponse.builder()
                .trophyId(trophy.getId())
                .name(trophy.getName())
                .type(trophy.getType())
                .achievedCount(snapshot.getAchievedCount())
                .metricValue(snapshot.getMetricValue())
                .build();
    }

    // 스냅샷 리스트 -> 응답 DTO 리스트 변환
    public static List<TrophyResponse> toTrophyResponseList(List<MemberTrophySnapshot> snapshots) {
        return snapshots.stream()
                .map(TrophyConverter::toTrophyResponse)
                .collect(Collectors.toList());
    }

    public static TrophyMetaResponse toTrophyMetaResponse(Trophy trophy) {
        return TrophyMetaResponse.builder()
                .trophyId(trophy.getId())
                .name(trophy.getName())
                .type(trophy.getType())
                .description(trophy.getDescription())
                .build();
    }

    public static List<TrophyMetaResponse> toTrophyMetaResponseList(List<Trophy> trophies) {
        return trophies.stream()
                .map(TrophyConverter::toTrophyMetaResponse)
                .collect(Collectors.toList());
    }
}
