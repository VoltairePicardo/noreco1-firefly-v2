package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.OtherDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Personal on 7/6/2015.
 */
public interface OtherDepositRepo extends JpaRepository<OtherDeposit, Integer> {
    @Query(value = "SELECT e.code FROM OtherDeposit e WHERE year = :year AND code LIKE :offAcro ORDER BY id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestOdCodeByYear(@Param("year") Integer year, @Param("offAcro") String offAcro);

    @Query(value = "SELECT " +
            "od.id, " +
            "od.checkNumber, " +
            "od.amount, " +
            "od.code, " +
            "od.voucherDate, " +
            "od.particulars, " +
            "a.title, " +
            "od.cleared, " +
            "od.FK_transactionId, " +
            "od.FK_accountId " +
            "FROM OtherDeposit od " +
            "INNER JOIN Account a ON a.id = od.FK_accountId", nativeQuery = true)
    public List<Object[]> findOtherDeposits();
}
