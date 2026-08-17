package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BankDeposit;
import com.noreco1.fireflyv2.model.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface BankDepositRepo extends JpaRepository<BankDeposit, Integer> {

    @Query(value = "SELECT e.code FROM BankDeposit e WHERE YEAR(createdAt) = :year  AND code LIKE '%BD%' AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestBankDepositCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    List<BankDeposit> findAllByReferenceNumberAndBankAccountIdAndDepositDateAndCashAmountAndCheckAmount(String refNo, int bankAccountId, Date date, BigDecimal cash, BigDecimal check);
    List<BankDeposit> findAllByReferenceNumberAndBankAccountIdAndDepositDateAndCashAmount(String refNo, int bankAccountId, Date date, BigDecimal cash);

}
