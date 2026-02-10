package org.umc.valuedi.domain.asset.repository.bank.bankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.umc.valuedi.domain.asset.entity.BankAccount;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long>, BankAccountRepositoryCustom {
    List<BankAccount> findByCodefConnection(CodefConnection codefConnection);
}
