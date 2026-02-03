package org.umc.valuedi.domain.asset.repository.bank.bankTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.asset.entity.BankTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long>, BankTransactionRepositoryCustom {
    List<BankTransaction> findByTrDateBetween(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT MAX(bt.trDate) FROM BankTransaction bt WHERE bt.bankAccount = :account")
    Optional<LocalDate> findLatestTransactionDateByAccount(@Param("account") BankAccount account);

    List<BankTransaction> findByBankAccountInAndTrDatetimeAfter(List<BankAccount> accounts, LocalDateTime startTime);
}
