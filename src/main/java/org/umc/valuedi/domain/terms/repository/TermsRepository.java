package org.umc.valuedi.domain.terms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.umc.valuedi.domain.terms.entity.Terms;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByIsActiveTrueAndIsRequiredTrue();

    @Query("""
    SELECT t
    FROM Terms t
    WHERE t.isActive = true
    ORDER BY
      CASE t.code
        WHEN 'AGE_14' THEN 1
        WHEN 'SERVICE' THEN 2
        WHEN 'SECURITY' THEN 3
        WHEN 'PRIVACY' THEN 4
        WHEN 'MARKETING' THEN 5
        ELSE 99
      END
""")
    List<Terms> findActiveTermsOrdered();
}
