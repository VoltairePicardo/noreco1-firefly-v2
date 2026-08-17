package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PettyCashFund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PettyCashFundRepo extends JpaRepository<PettyCashFund, Integer> {
    PettyCashFund findByAccountNo(Integer accountNo);
    PettyCashFund findOneByOfficeId(Integer office);
    Page<PettyCashFund> findByDescriptionContainingIgnoreCase(String query, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT cv.voucherDate AS date, cv.code AS code, cv.particulars AS description, SUM(sl.debit - sl.credit) AS amount2, 0 AS amount FROM CheckVoucher cv " +
            "INNER JOIN SubLedger sl ON cv.FK_transactionId = sl.FK_transactionId " +
            "INNER JOIN pettycashfund pcf ON pcf.FK_accountNo = sl.FK_accountNo " +
            "WHERE pcf.id = :pcfId " +
            "AND cv.FK_documentStatusId = :docStatId " +
            "AND cv.voucherDate >= :from " +
            "AND cv.voucherDate <= :to " +
            "GROUP BY sl.FK_accountNo " +
            "UNION " +
            "SELECT pct.pettyCashDate AS date, pct.code AS code, pct.purpose AS description, 0 AS amount2, amount FROM pettycashtrans pct " +
            "INNER JOIN pettycashfund pcf ON pcf.id = pct.FK_pettyCashFundId " +
            "WHERE pcf.id = :pcfId " +
            "AND pct.FK_documentStatusId = :docStatId " +
            "AND pct.voucherDate >= :from " +
            "AND pct.voucherDate <= :to " +
            ") AS t1 ORDER BY date",
            nativeQuery = true)
    List<Object[]> findVouchersForLedger(@Param("from") String from, @Param("to") String to, @Param("docStatId") Integer docStatId, @Param("pcfId") Integer pcfId);

    @Query(value = "SELECT * FROM (SELECT cv.voucherDate AS date, cv.code AS code, cv.particulars AS description, SUM(sl.debit - sl.credit) AS amount2, 0 AS amount FROM CheckVoucher cv " +
            "INNER JOIN SubLedger sl ON cv.FK_transactionId = sl.FK_transactionId " +
            "INNER JOIN pettycashfund pcf ON pcf.FK_accountNo = sl.FK_accountNo " +
            "WHERE pcf.id = :pcfId " +
            "AND cv.FK_documentStatusId NOT IN(:ids) " +
            "AND cv.voucherDate >= :from " +
            "AND cv.voucherDate <= :to " +
            "GROUP BY sl.FK_accountNo " +
            "UNION " +
            "SELECT pct.pettyCashDate AS date, pct.code AS code, pct.purpose AS description, 0 AS amount2, amount FROM pettycashtrans pct " +
            "INNER JOIN pettycashfund pcf ON pcf.id = pct.FK_pettyCashFundId " +
            "WHERE pcf.id = :pcfId " +
            "AND pct.FK_documentStatusId NOT IN(:ids) " +
            "AND pct.voucherDate >= :from " +
            "AND pct.voucherDate <= :to " +
            ") AS t1 ORDER BY date",
            nativeQuery = true)
    List<Object[]> findVouchersDocumentStatusNotInForLedger(@Param("from") String from, @Param("to") String to, @Param("ids") Collection<Integer> ids, @Param("pcfId") Integer pcfId);
}
