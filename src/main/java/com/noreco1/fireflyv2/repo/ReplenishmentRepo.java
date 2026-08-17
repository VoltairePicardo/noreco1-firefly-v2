package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.Replenishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Personal on 9/7/2016.
 */
public interface ReplenishmentRepo extends JpaRepository<Replenishment, Integer> {
    @Query(value = "SELECT SUM(debit) from Replenishment " +
            "JOIN SubLedger ON Replenishment.FK_checkVoucherTransactionId = SubLedger.FK_transactionId " +
            "JOIN PettyCashFund ON PettyCashFund.FK_accountNo = SubLedger.FK_accountNo " +
            "WHERE PettyCashFund.id = :pcfId AND Replenishment.createdAt < :date LIMIT 1", nativeQuery = true)
    List<Object[]> findTotalBeforeDate(@Param("date") String date, @Param("pcfId") Integer pcfId);
}
