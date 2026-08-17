package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.TemporaryGeneralLedger;
import com.noreco1.fireflyv2.model.TemporarySubLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TemporarySubLedgerRepo extends JpaRepository<TemporarySubLedger, Integer> {

    @Query(value = "SELECT " +
            "TemporarySubLedger.id, " +
            "TemporarySubLedger.FK_accountNo, " +
            "slentity.name, " +
            "SegmentAccount.FK_accountId, " +
            "SUM(COALESCE(TemporarySubLedger.debit,0) + COALESCE(TemporarySubLedger.credit, 0)) AS amount " +
            "FROM TemporarySubLedger " +
            "JOIN SegmentAccount ON TemporarySubLedger.FK_segmentAccountId = SegmentAccount.id " +
            "JOIN slentity ON TemporarySubLedger.FK_accountNo = slentity.accountNo " +
            "WHERE TemporarySubLedger.FK_temporaryBatchId = :tempBatchId " +
            "AND SegmentAccount.FK_accountId = :accountId " +
            "GROUP BY TemporarySubLedger.FK_accountNo", nativeQuery = true)
    public List<Object[]> findByTempBatchIdAndAccountId(@Param("tempBatchId") Integer tempBatchId, @Param("accountId") Integer accountId);

    TemporarySubLedger findOneByTemporaryBatchIdAndSegmentAccountIdAndCreditIsNotNullAndCreditGreaterThan(Integer tempBatchId, Integer segmentAccountId,  BigDecimal amount);
}
