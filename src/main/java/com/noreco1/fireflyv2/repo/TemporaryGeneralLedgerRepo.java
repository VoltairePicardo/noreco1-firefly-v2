package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TemporaryGeneralLedger;
import com.noreco1.fireflyv2.model.TemporarySubLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface TemporaryGeneralLedgerRepo extends JpaRepository<TemporaryGeneralLedger, Integer> {

    void deleteAllByTemporaryBatchId(Integer temporaryBatchId);

    @Query(value = "SELECT " +
            "    gl.debit, " +
            "    gl.credit, " +
            "    Account.id, " +
            "    Account.code, " +
            "    Account.title, " +
            "    Account.hasSL " +
            "FROM ( " +
            "    SELECT " +
            "        Account.id AS accountId, " +
            "        SUM(COALESCE(TemporaryGeneralLedger.debit, 0)) AS debit, " +
            "        SUM(COALESCE(TemporaryGeneralLedger.credit, 0)) AS credit, " +
            "        CASE WHEN SUM(COALESCE(TemporaryGeneralLedger.debit, 0)) = 0 THEN 1 ELSE 0 END AS side " +
            "    FROM TemporaryGeneralLedger " +
            "    JOIN Account " +
            "        ON TemporaryGeneralLedger.FK_accountId = Account.id " +
            "    WHERE TemporaryGeneralLedger.FK_temporaryBatchId = :tempBatchId " +
            "    GROUP BY Account.id " +
            ") AS gl " +
            "JOIN Account " +
            "    ON gl.accountId = Account.id " +
            "ORDER BY gl.side, Account.code; ", nativeQuery = true)
    List<Object[]> findByBatchId(@Param("tempBatchId") Integer tempBatchId);

    TemporaryGeneralLedger findOneByTemporaryBatchIdAndSegmentAccountIdAndCreditIsNotNullAndCreditGreaterThan(Integer tempBatchId, Integer segmentAccountId, BigDecimal amount);
    List<TemporaryGeneralLedger> findByTemporaryBatchId(Integer tempBatchId);
}
