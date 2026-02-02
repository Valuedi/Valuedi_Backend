package org.umc.valuedi.domain.savings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.savings.entity.Savings;

import java.util.Optional;

public interface SavingsRepository extends JpaRepository<Savings, Long> {

    Optional<Savings> findByFinPrdtCd(String finPrdtCd);
}
