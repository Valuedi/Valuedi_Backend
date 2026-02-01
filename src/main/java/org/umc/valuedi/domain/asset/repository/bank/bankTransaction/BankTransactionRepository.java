package org.umc.valuedi.domain.asset.repository.bank.bankTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.BankTransaction;

import java.time.LocalDate;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, BankTransactionRepositoryCustom {
    List<BankTransaction> findByTrDateBetween(LocalDate fromDate, LocalDate toDate);
}
