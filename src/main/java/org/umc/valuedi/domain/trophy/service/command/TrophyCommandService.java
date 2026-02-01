package org.umc.valuedi.domain.trophy.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.trophy.dto.TrophyCalculationDto;
import org.umc.valuedi.domain.trophy.entity.MemberTrophy;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.domain.trophy.repository.MemberTrophyRepository;
import org.umc.valuedi.domain.trophy.repository.MemberTrophySnapshotRepository;
import org.umc.valuedi.domain.trophy.repository.TrophyAnalysisRepository;
import org.umc.valuedi.domain.trophy.repository.TrophyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrophyCommandService {

    private final TrophyRepository trophyRepository;
    private final MemberTrophyRepository memberTrophyRepository;
    private final MemberTrophySnapshotRepository snapshotRepository;
    private final TrophyAnalysisRepository analysisRepository;
    private final MemberRepository memberRepository;

    public void calculateAndSnapshot(Long memberId, PeriodType periodType, String periodKey, LocalDateTime start, LocalDateTime end) {
        // 1. 통계 계산 (Mock)
        TrophyCalculationDto stats = analysisRepository.calculateStatsMock(memberId, start, end);

        // 2. 달성 여부 체크 및 저장
        List<Trophy> allTrophies = trophyRepository.findAll();

        // Member 프록시 조회 (반복 조회 방지)
        Member member = memberRepository.getReferenceById(memberId);

        for (Trophy trophy : allTrophies) {
            if (trophy.getType().isAchieved(stats)) {
                saveSnapshotAndAccumulate(member, trophy, periodType, periodKey, stats);
            }
        }
    }

    private void saveSnapshotAndAccumulate(Member member, Trophy trophy, PeriodType periodType, String periodKey, TrophyCalculationDto stats) {
        // 1. 스냅샷 저장 (Upsert)
        MemberTrophySnapshot snapshot = snapshotRepository.findSnapshot(
                member, trophy, periodType, periodKey
        ).orElseGet(() -> new MemberTrophySnapshot(member, trophy, periodType, periodKey, 0, "0"));

        String metricValue = String.valueOf(stats.getTotalAmount());
        snapshot.updateSnapshot(1, metricValue);
        snapshotRepository.save(snapshot);

        // 2. 누적 테이블 업데이트
        MemberTrophy memberTrophy = memberTrophyRepository.findMemberTrophy(member, trophy)
                .orElseGet(() -> new MemberTrophy(member, trophy));

        memberTrophy.accumulate(1, LocalDateTime.now());
        memberTrophyRepository.save(memberTrophy);
    }
}
