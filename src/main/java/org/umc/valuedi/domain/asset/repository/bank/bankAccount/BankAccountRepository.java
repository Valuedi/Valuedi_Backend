package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long>, BankAccountRepositoryCustom {
}
