package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.savings.entity.SavingsOption;

import java.util.List;

public interface SavingsOptionRepository extends JpaRepository<SavingsOption, Long> {

    @Query("""
        select so
        from SavingsOption so
        join fetch so.savings s
        where so.id in :ids
    """)
    List<SavingsOption> findAllByIdInFetchSavings(
            @Param("ids") List<Long> ids
    );

    @Query("""
        select so
        from SavingsOption so
        join fetch so.savings s
        order by so.intrRate2 desc nulls last, so.intrRate desc nulls last
    """)
    List<SavingsOption> findCandidates(
            Pageable pageable
    );

    // G형(공격): 우대금리 우선 정렬
    @Query("""
        select so
        from SavingsOption so
        join fetch so.savings s
        where (:rsrvType is null or so.rsrvType = :rsrvType)
          and (:minTrm is null or so.saveTrm >= :minTrm)
          and (:maxTrm is null or so.saveTrm <= :maxTrm)
        order by so.intrRate2 desc nulls last, so.intrRate desc nulls last
    """)
    List<SavingsOption> findCandidatesOrderByRate2(
            @Param("rsrvType") String rsrvType,
            @Param("minTrm") Integer minTrm,
            @Param("maxTrm") Integer maxTrm,
            Pageable pageable
    );

    // C형(보수): 기본금리 우선 정렬
    @Query("""
        select so
        from SavingsOption so
        join fetch so.savings s
        where (:rsrvType is null or so.rsrvType = :rsrvType)
          and (:minTrm is null or so.saveTrm >= :minTrm)
          and (:maxTrm is null or so.saveTrm <= :maxTrm)
        order by so.intrRate desc nulls last, so.intrRate2 desc nulls last
    """)
    List<SavingsOption> findCandidatesOrderByRate(
            @Param("rsrvType") String rsrvType,
            @Param("minTrm") Integer minTrm,
            @Param("maxTrm") Integer maxTrm,
            Pageable pageable
    );
}
