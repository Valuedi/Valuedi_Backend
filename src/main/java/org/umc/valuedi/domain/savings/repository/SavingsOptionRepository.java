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
}
