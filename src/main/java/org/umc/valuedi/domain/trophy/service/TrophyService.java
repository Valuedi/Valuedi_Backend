package org.umc.valuedi.domain.trophy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.member.repository.MemberRepository;
import org.umc.valuedi.domain.trophy.dto.TrophyCalculationDto;
import org.umc.valuedi.domain.trophy.converter.TrophyConverter;
import org.umc.valuedi.domain.trophy.dto.response.TrophyMetaResponse;
import org.umc.valuedi.domain.trophy.dto.response.TrophyResponse;
import org.umc.valuedi.domain.trophy.entity.MemberTrophy;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.PeriodType;
import org.umc.valuedi.domain.trophy.exception.TrophyException;
import org.umc.valuedi.domain.trophy.exception.code.TrophyErrorCode;
import org.umc.valuedi.domain.trophy.repository.MemberTrophyRepository;
import org.umc.valuedi.domain.trophy.repository.MemberTrophySnapshotRepository;
import org.umc.valuedi.domain.trophy.repository.TrophyAnalysisRepository;
import org.umc.valuedi.domain.trophy.repository.TrophyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrophyService {

    private final TrophyRepository trophyRepository;
    private final MemberTrophyRepository memberTrophyRepository;
    private final MemberTrophySnapshotRepository snapshotRepository;
    private final TrophyAnalysisRepository analysisRepository;
    private final MemberRepository memberRepository;

    /**
     * 트로피 달성 여부 계산 및 스냅샷 저장 (배치 또는 API에서 호출)
     */
    @Transactional
    public void calculateAndSnapshot(Long memberId, PeriodType periodType, String periodKey, LocalDateTime start, LocalDateTime end) {
        // 1. 해당 기간의 소비 통계 계산 (QueryDSL)
        TrophyCalculationDto stats = analysisRepository.calculateStatsMock(memberId, start, end);

        // 2. 모든 트로피 타입에 대해 달성 여부 체크
        List<Trophy> allTrophies = trophyRepository.findAll();

        for (Trophy trophy : allTrophies) {
            if (trophy.getType().isAchieved(stats)) {
                saveSnapshotAndAccumulate(memberId, trophy, periodType, periodKey, stats);
            }
        }
    }

    @Transactional
    protected void saveSnapshotAndAccumulate(Long memberId, Trophy trophy, PeriodType periodType, String periodKey, TrophyCalculationDto stats) {

        Member member = memberRepository.getReferenceById(memberId);
        // 2. 스냅샷 저장 (Upsert)
        MemberTrophySnapshot snapshot = snapshotRepository.findByMemberIdAndTrophyAndPeriodTypeAndPeriodKey(
                member, trophy, periodType, periodKey
        ).orElseGet(() -> new MemberTrophySnapshot(member, trophy, periodType, periodKey, 0, "0"));

        // 메트릭 값 포맷팅 (예: 금액 콤마 등은 여기서 하거나 프론트에서 처리. 여기선 Raw 값 저장 추천)
        String metricValue = String.valueOf(stats.getTotalAmount());
        snapshot.updateSnapshot(1, metricValue); // 횟수는 로직에 따라 1 또는 N
        snapshotRepository.save(snapshot);

        // 3. 누적 테이블 업데이트
        MemberTrophy memberTrophy = memberTrophyRepository.findByMemberIdAndTrophy(member, trophy)
                .orElseGet(() -> new MemberTrophy(member, trophy));

        memberTrophy.accumulate(1, LocalDateTime.now());
        memberTrophyRepository.save(memberTrophy);
    }

    public List<TrophyResponse> getMyTrophies(Long memberId, PeriodType periodType, String periodKey) {
        // 1. 회원 검증 및 객체 조회 (필수)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new TrophyException(TrophyErrorCode.MEMBER_NOT_FOUND));

        // 2. Member 객체를 넘겨서 조회
        List<MemberTrophySnapshot> snapshots = snapshotRepository.findAllByMemberIdAndPeriodTypeAndPeriodKey(member, periodType, periodKey);

        return TrophyConverter.toTrophyResponseList(snapshots);
    }

    public List<TrophyMetaResponse> getAllTrophies() {
        List<Trophy> trophies = trophyRepository.findAll();
        return TrophyConverter.toTrophyMetaResponseList(trophies);
    }

}
