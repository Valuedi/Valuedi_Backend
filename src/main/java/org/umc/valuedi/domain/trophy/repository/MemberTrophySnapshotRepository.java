package org.umc.valuedi.domain.trophy.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.member.entity.Member;
import org.umc.valuedi.domain.trophy.entity.MemberTrophySnapshot;
import org.umc.valuedi.domain.trophy.entity.Trophy;
import org.umc.valuedi.domain.trophy.enums.PeriodType;

import java.util.List;
import java.util.Optional;

public interface MemberTrophySnapshotRepository extends JpaRepository<MemberTrophySnapshot, Long> {

    // JPQL로 명시적 매핑 (필드명 memberId가 Member 타입임을 명시)
    @Query("SELECT s FROM MemberTrophySnapshot s WHERE s.member = :member AND s.trophy = :trophy AND s.periodType = :periodType AND s.periodKey = :periodKey")
    Optional<MemberTrophySnapshot> findSnapshot(
            @Param("member") Member member,
            @Param("trophy") Trophy trophy,
            @Param("periodType") PeriodType periodType,
            @Param("periodKey") String periodKey
    );

    @Query("SELECT s FROM MemberTrophySnapshot s WHERE s.member = :member AND s.periodType = :periodType AND s.periodKey = :periodKey")
    List<MemberTrophySnapshot> findAllSnapshots(
            @Param("member") Member member,
            @Param("periodType") PeriodType periodType,
            @Param("periodKey") String periodKey
    );
}
