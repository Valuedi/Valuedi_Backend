package org.umc.valuedi.domain.terms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.terms.entity.Terms;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    //List<Terms> findAllByIsActiveTrue();

    @Query(value = """
        SELECT *
        FROM terms
        WHERE is_active = true
        ORDER BY FIELD(code, 'AGE_14','SERVICE','SECURITY','PRIVACY','MARKETING')
    """, nativeQuery = true)
    List<Terms> findActiveTermsOrdered();
}
