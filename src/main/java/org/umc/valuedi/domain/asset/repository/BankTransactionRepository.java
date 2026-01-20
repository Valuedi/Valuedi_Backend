package org.umc.valuedi.domain.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, BankTransactionRepositoryCustom {
}
