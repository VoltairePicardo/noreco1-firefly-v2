package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BankAccountRepo extends JpaRepository<BankAccount, Integer> {

     Page<BankAccount> findAllByBankNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(String bankName, String accountNumber, Pageable pageable);

     @Transactional(readOnly = true)
     @Query(value = "SELECT * FROM BankAccount ba " +
             "WHERE ba.id NOT IN (SELECT cc.FK_bankAccountId FROM CheckConfig cc) " +
             "AND ba.FK_bankTransactionTypeId = :bankTransactionType ", nativeQuery = true)
     List<BankAccount> getAllBankAccountsWithCheckOnly(@Param("bankTransactionType") Integer bankTransactionType);

     List<BankAccount> findAllByBankIdAndBankTransactionTypeId(Integer bankId, Integer bankTransactionTypeId);
     List<BankAccount> findAllByBankId(Integer bankId);

     BankAccount findFirstByBankIdAndAccountId(Integer bankId, Integer accountId);

}
