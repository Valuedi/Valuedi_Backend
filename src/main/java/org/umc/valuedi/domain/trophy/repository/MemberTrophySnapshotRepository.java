package org.umc.valuedi.domain.trophy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.PeriodType;

import java.util.List;
import java.util.Optional;

public interface MemberTrophySnapshotRepository extends JpaRepository<MemberTrophySnapshot, Long> {

    Optional<MemberTrophySnapshot> findByMemberIdAndTrophyAndPeriodTypeAndPeriodKey(
            Long memberId, Trophy trophy, PeriodType periodType, String periodKey
    );

    List<MemberTrophySnapshot> findAllByMemberIdAndPeriodTypeAndPeriodKey(
            Long memberId, PeriodType periodType, String periodKey
    );
}
