package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.savings.entity.SavingsOption;

import java.util.List;

public interface SavingsOptionRespository extends JpaRepository<SavingsOption, Long> {

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
        where (:rsrvType is null or so.rsrvType = :rsrvType)
          and (:saveTrm is null or so.saveTrm = :saveTrm)
    """)
    List<SavingsOption> findCandidates(
            @Param("rsrvType") String rsrvType,
            @Param("saveTrm") Integer saveTrm,
            Pageable pageable
    );
}
