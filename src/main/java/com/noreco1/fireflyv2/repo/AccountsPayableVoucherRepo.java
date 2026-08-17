package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AccountsPayableVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface AccountsPayableVoucherRepo extends JpaRepository<AccountsPayableVoucher, Integer> {
    @Query(value = "SELECT e.code FROM AccountsPayableVoucher e WHERE year = :year  AND code LIKE '%APV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestApvCodeByYear(@Param("year") Integer year);
    public List<AccountsPayableVoucher> findByDocumentStatusId(Integer statusId);

    @Query(value = "SELECT " +
            "id, " +
            "code, " +
            "particulars, " +
            "voucherDate, " +
            "FK_transactionId, " +
            "slentity.name " +
            "FROM AccountsPayableVoucher " +
            "JOIN slentity ON AccountsPayableVoucher.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE voucherDate >= :from AND voucherDate <= :to " +
            "AND FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "id, " +
            "code, " +
            "particulars, " +
            "voucherDate, " +
            "FK_transactionId, " +
            "slentity.name " +
            "FROM AccountsPayableVoucher " +
            "JOIN slentity ON AccountsPayableVoucher.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE voucherDate >= :from AND voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "id, " +
            "code, " +
            "particulars, " +
            "voucherDate, " +
            "FK_transactionId, " +
            "slentity.name " +
            "FROM AccountsPayableVoucher " +
            "JOIN slentity ON AccountsPayableVoucher.FK_vendorAccountNo = slentity.accountNo " +
            "WHERE voucherDate >= :from AND voucherDate <= :to " +
            "AND FK_documentStatusId = :documentStatusId " +
            "AND FK_officeId = :officeId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

    @Query(value = "SELECT  " +
            "SUM(generals.glDebit) AS glDebit, " +
            "SUM(generals.glCredit) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  " +
            "subs.accountNo,  " +
            "SegmentAccount.accountCode,  " +
            "Account.title,  " +
            "slentity.name,  " +
            "if (glDebit-glCredit > 0 , 1, 2) as side  " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "SUM(GeneralLedger.debit) AS glDebit, " +
            "SUM(GeneralLedger.credit) AS glCredit " +
            "FROM GeneralLedger " +
            "JOIN AccountsPayableVoucher on GeneralLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to) AND AccountsPayableVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit " +
            "FROM SubLedger " +
            "JOIN AccountsPayableVoucher on SubLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to) AND AccountsPayableVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT  " +
            "SUM(generals.glDebit) AS glDebit, " +
            "SUM(generals.glCredit) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  " +
            "subs.accountNo,  " +
            "SegmentAccount.accountCode,  " +
            "Account.title,  " +
            "slentity.name,  " +
            "if (glDebit-glCredit > 0 , 1, 2) as side, " +
            "if (subs.id IS NOT null , 1, 0) as isSl " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "SUM(GeneralLedger.debit) AS glDebit, " +
            "SUM(GeneralLedger.credit) AS glCredit " +
            "FROM GeneralLedger " +
            "JOIN AccountsPayableVoucher on GeneralLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to)  " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "subledger.id " +
            "FROM SubLedger " +
            "JOIN AccountsPayableVoucher on SubLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to) " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT  " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  " +
            "subs.accountNo,  " +
            "SegmentAccount.accountCode,  " +
            "Account.title,  " +
            "slentity.name,  " +
            "if (glDebit-glCredit > 0 , 1, 2) as side  " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit," +
            "IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit  " +
            "FROM GeneralLedger " +
            "JOIN AccountsPayableVoucher on GeneralLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to) AND AccountsPayableVoucher.FK_documentStatusId = :documentStatusId AND AccountsPayableVoucher.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, " +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit  " +
            "FROM SubLedger " +
            "JOIN AccountsPayableVoucher on SubLedger.FK_transactionId = AccountsPayableVoucher.FK_transactionId " +
            "WHERE (AccountsPayableVoucher.voucherDate >= :from AND AccountsPayableVoucher.voucherDate <= :to) AND AccountsPayableVoucher.FK_documentStatusId = :documentStatusId AND AccountsPayableVoucher.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

    public AccountsPayableVoucher findOneByTransactionId(Integer transId);

    public List<AccountsPayableVoucher> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    public List<AccountsPayableVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    public List<AccountsPayableVoucher> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    public List<AccountsPayableVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);

    @Query(value = "SELECT doc.* " +
            "FROM AccountsPayableVoucher doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<AccountsPayableVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    AccountsPayableVoucher findFirstByOrderByIdAsc();
    @Query(value = "SELECT v.name, COALESCE(t.amount1,0),  COALESCE(t1.amount2,0), COALESCE(t2.amount3,0), COALESCE(t3.amount4,0), COALESCE(t4.amount5,0), (COALESCE(t.amount1,0) + COALESCE(t1.amount2,0) + COALESCE(t2.amount3,0) + COALESCE(t3.amount4,0) + COALESCE(t4.amount5,0)) AS total FROM " +
            "(SELECT apv.FK_vendorAccountNo, s.name FROM AccountsPayableVoucher apv INNER JOIN Supplier s ON s.FK_accountNo = apv.FK_vendorAccountNo WHERE apv.dueDate <= :cutOffDate AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId FROM CheckVoucherApv) AND apv.FK_documentStatusId = 7 GROUP BY apv.FK_vendorAccountNo) AS v LEFT JOIN " +
            "(SELECT SUM(apv.amount) AS amount1, apv.FK_vendorAccountNo FROM AccountsPayableVoucher apv WHERE :cutOffDate <= apv.dueDate AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId FROM CheckVoucherApv) AND apv.FK_documentStatusId = 7 GROUP BY apv.FK_vendorAccountNo) AS t ON v.FK_vendorAccountNo = t.FK_vendorAccountNo LEFT JOIN " +
            "(SELECT SUM(apv.amount) AS amount2, apv.FK_vendorAccountNo FROM AccountsPayableVoucher apv WHERE datediff(:cutOffDate, apv.dueDate) BETWEEN 1 AND 30 AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId AND apv.FK_documentStatusId = 7 FROM CheckVoucherApv) GROUP BY apv.FK_vendorAccountNo) AS t1 ON v.FK_vendorAccountNo = t1.FK_vendorAccountNo  LEFT JOIN " +
            "(SELECT SUM(apv.amount) AS amount3, apv.FK_vendorAccountNo FROM AccountsPayableVoucher apv WHERE datediff(:cutOffDate, apv.dueDate) BETWEEN 31 AND 60 AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId AND apv.FK_documentStatusId = 7 FROM CheckVoucherApv) GROUP BY apv.FK_vendorAccountNo) AS t2 ON v.FK_vendorAccountNo = t2.FK_vendorAccountNo  LEFT JOIN " +
            "(SELECT SUM(apv.amount) AS amount4, apv.FK_vendorAccountNo FROM AccountsPayableVoucher apv WHERE datediff(:cutOffDate, apv.dueDate) BETWEEN 61 AND 90 AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId AND apv.FK_documentStatusId = 7 FROM CheckVoucherApv) GROUP BY apv.FK_vendorAccountNo) AS t3 ON v.FK_vendorAccountNo = t3.FK_vendorAccountNo  LEFT JOIN " +
            "(SELECT SUM(apv.amount) AS amount5, apv.FK_vendorAccountNo FROM AccountsPayableVoucher apv WHERE datediff(:cutOffDate, apv.dueDate) > 90 AND apv.id NOT IN (SELECT FK_accountsPayableVoucherId FROM CheckVoucherApv) AND apv.FK_documentStatusId = 7 GROUP BY apv.FK_vendorAccountNo) AS t4 ON v.FK_vendorAccountNo = t4.FK_vendorAccountNo " +
            "ORDER BY v.name", nativeQuery = true)
    List<Object[]> findForAging(@Param("cutOffDate") String cutOffDate);

    List<AccountsPayableVoucher> findByDocumentStatusIdNotIn(List<Integer> integers);
    AccountsPayableVoucher findFirstByOrderByIdDesc();

    @Query(value = "SELECT apv.* FROM AccountsPayableVoucher apv " +
            "WHERE apv.FK_documentStatusId = :documentStatusId " +
            "AND ( " +
            "  (COALESCE(apv.forInstallment, 0) = 0 AND apv.id NOT IN ( " +
            "      SELECT cva.FK_accountsPayableVoucherId " +
            "      FROM CheckVoucherApv cva " +
            "      INNER JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "      WHERE cv.FK_documentStatusId NOT IN (8, 26) " +
            "  )) " +
            "  OR " +
            "  (COALESCE(apv.forInstallment, 0) = 1 AND EXISTS ( " +
            "      SELECT 1 FROM AccountsPayableVoucherInstallmentDetail apvid " +
            "      WHERE apvid.FK_accountsPayableVoucherId = apv.id " +
            "      AND NOT EXISTS ( " +
            "          SELECT 1 FROM CheckVoucherApvPaidInstallment cvapi " +
            "          WHERE cvapi.FK_accountsPayableVoucherInstallmentDetailId = apvid.id " +
            "      ) " +
            "  )) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM AccountsPayableVoucher apv " +
                    "WHERE apv.FK_documentStatusId = :documentStatusId " +
                    "AND ( " +
                    "  (COALESCE(apv.forInstallment, 0) = 0 AND apv.id NOT IN ( " +
                    "      SELECT cva.FK_accountsPayableVoucherId " +
                    "      FROM CheckVoucherApv cva " +
                    "      INNER JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
                    "      WHERE cv.FK_documentStatusId NOT IN (8, 26) " +
                    "  )) " +
                    "  OR " +
                    "  (COALESCE(apv.forInstallment, 0) = 1 AND EXISTS ( " +
                    "      SELECT 1 FROM AccountsPayableVoucherInstallmentDetail apvid " +
                    "      WHERE apvid.FK_accountsPayableVoucherId = apv.id " +
                    "      AND NOT EXISTS ( " +
                    "          SELECT 1 FROM CheckVoucherApvPaidInstallment cvapi " +
                    "          WHERE cvapi.FK_accountsPayableVoucherInstallmentDetailId = apvid.id " +
                    "      ) " +
                    "  )) " +
                    ") ",
            nativeQuery = true)
    Page<AccountsPayableVoucher> findAllByDocumentStatusForCv(@Param("documentStatusId") Integer documentStatusId,
                                                              Pageable pageable);

    @Query(value = "SELECT apv.* FROM AccountsPayableVoucher apv " +
            "WHERE apv.FK_documentStatusId = :documentStatusId " +
            "AND apv.code LIKE :query " +
            "AND ( " +
            "  (COALESCE(apv.forInstallment, 0) = 0 AND apv.id NOT IN ( " +
            "      SELECT cva.FK_accountsPayableVoucherId " +
            "      FROM CheckVoucherApv cva " +
            "      INNER JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
            "      WHERE cv.FK_documentStatusId NOT IN (8, 26) " +
            "  )) " +
            "  OR " +
            "  (COALESCE(apv.forInstallment, 0) = 1 AND EXISTS ( " +
            "      SELECT 1 FROM AccountsPayableVoucherInstallmentDetail apvid " +
            "      WHERE apvid.FK_accountsPayableVoucherId = apv.id " +
            "      AND NOT EXISTS ( " +
            "          SELECT 1 FROM CheckVoucherApvPaidInstallment cvapi " +
            "          WHERE cvapi.FK_accountsPayableVoucherInstallmentDetailId = apvid.id " +
            "      ) " +
            "  )) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM AccountsPayableVoucher apv " +
                    "WHERE apv.FK_documentStatusId = :documentStatusId " +
                    "AND apv.code LIKE :query " +
                    "AND ( " +
                    "  (COALESCE(apv.forInstallment, 0) = 0 AND apv.id NOT IN ( " +
                    "      SELECT cva.FK_accountsPayableVoucherId " +
                    "      FROM CheckVoucherApv cva " +
                    "      INNER JOIN CheckVoucher cv ON cv.id = cva.FK_checkVoucherId " +
                    "      WHERE cv.FK_documentStatusId NOT IN (8, 26) " +
                    "  )) " +
                    "  OR " +
                    "  (COALESCE(apv.forInstallment, 0) = 1 AND EXISTS ( " +
                    "      SELECT 1 FROM AccountsPayableVoucherInstallmentDetail apvid " +
                    "      WHERE apvid.FK_accountsPayableVoucherId = apv.id " +
                    "      AND NOT EXISTS ( " +
                    "          SELECT 1 FROM CheckVoucherApvPaidInstallment cvapi " +
                    "          WHERE cvapi.FK_accountsPayableVoucherInstallmentDetailId = apvid.id " +
                    "      ) " +
                    "  )) " +
                    ") ",
            nativeQuery = true)
    Page<AccountsPayableVoucher> findAllByQueryAndDocumentStatusForCv(@Param("query") String query,
                                                                      @Param("documentStatusId") Integer documentStatusId,
                                                                      Pageable pageable);
}
