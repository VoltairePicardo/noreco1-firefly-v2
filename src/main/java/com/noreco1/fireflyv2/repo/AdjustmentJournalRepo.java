package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.AdjustmentJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 11/15/2016.
 */
public interface AdjustmentJournalRepo extends JpaRepository<AdjustmentJournal, Integer> {
    @Query(value = "SELECT e.code FROM AdjustmentJournal e WHERE year = :year AND code LIKE '%AV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    public Object findLatestAjCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT " +
            "AdjustmentJournal.id, " +
            "AdjustmentJournal.code, " +
            "AdjustmentJournal.explanation, " +
            "AdjustmentJournal.voucherDate, " +
            "AdjustmentJournal.FK_transactionId " +
            "FROM AdjustmentJournal " +
            "WHERE AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to " +
            "AND AdjustmentJournal.FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "AdjustmentJournal.id, " +
            "AdjustmentJournal.code, " +
            "AdjustmentJournal.explanation, " +
            "AdjustmentJournal.voucherDate, " +
            "AdjustmentJournal.FK_transactionId " +
            "FROM AdjustmentJournal " +
            "WHERE AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "AdjustmentJournal.id, " +
            "AdjustmentJournal.code, " +
            "AdjustmentJournal.explanation, " +
            "AdjustmentJournal.voucherDate, " +
            "AdjustmentJournal.FK_transactionId " +
            "FROM AdjustmentJournal " +
            "WHERE AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to " +
            "AND AdjustmentJournal.FK_documentStatusId = :documentStatusId " +
            "AND AdjustmentJournal.FK_officeId = :officeId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

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
            "if (glDebit-glCredit > 0 , 1, 2) as side,  " +
            "if (subs.id IS NOT null , 1, 0) as isSl " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "SUM(GeneralLedger.debit) AS glDebit, " +
            "SUM(GeneralLedger.credit) AS glCredit " +
            "FROM GeneralLedger " +
            "JOIN AdjustmentJournal on GeneralLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) AND AdjustmentJournal.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN AdjustmentJournal on SubLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) AND AdjustmentJournal.FK_documentStatusId = :documentStatusId " +
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
            "if (glDebit-glCredit > 0 , 1, 2) as side,  " +
            "if (subs.id IS NOT null , 1, 0) as isSl " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "SUM(GeneralLedger.debit) AS glDebit, " +
            "SUM(GeneralLedger.credit) AS glCredit " +
            "FROM GeneralLedger " +
            "JOIN AdjustmentJournal on GeneralLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN AdjustmentJournal on SubLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) " +
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
            "JOIN AdjustmentJournal on GeneralLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) AND AdjustmentJournal.FK_documentStatusId = :documentStatusId AND AdjustmentJournal.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, " +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit  " +
            "FROM SubLedger " +
            "JOIN AdjustmentJournal on SubLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "WHERE (AdjustmentJournal.voucherDate >= :from AND AdjustmentJournal.voucherDate <= :to) AND AdjustmentJournal.FK_documentStatusId = :documentStatusId AND AdjustmentJournal.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId,  @Param("officeId") Integer officeId);

    public AdjustmentJournal findOneByTransactionId(Integer transId);
    public List<AdjustmentJournal> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    public List<AdjustmentJournal> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    public List<AdjustmentJournal> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    List<AdjustmentJournal> findByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(Date from, Date to, Collection<Integer> documentStatusIds);
    public AdjustmentJournal findFirstByOrderByIdAsc();

    @Query(value = "SELECT doc.* " +
            "FROM AdjustmentJournal doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<AdjustmentJournal> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "   SUM(COALESCE(debit,0)) AS debit, " +
            "   SUM(COALESCE(credit,0)) AS credit, " +
            "   accountId, " +
            "   code, " +
            "   title, " +
            "   hasSL " +
            "FROM ( " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN AdjustmentJournal j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId  " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN AccountsPayableVoucher j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId   " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN CheckVoucher j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN MaterialIssueRegister j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            "       " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN SalesVoucher j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN CashReceipts j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN BankDeposit j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            "    " +
            "UNION ALL " +
            " " +
            "SELECT " +
            "   Account.id as accountId, " +
            "   Account.code, " +
            "   Account.title, " +
            "   Account.hasSL, " +
            "   SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "   SUM(COALESCE(GeneralLedger.credit,0)) AS credit " +
            "   FROM GeneralLedger " +
            "   JOIN JournalVoucher j ON GeneralLedger.FK_transactionId = j.FK_transactionId " +
            "   JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "   JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "   WHERE Account.FK_accountTypeId IN (4,5) AND j.voucherDate <= :asOfDate AND j.FK_documentStatusId = 7 " + /*approved*/
            "   GROUP BY SegmentAccount.FK_accountId " +
            " " +
            ") as entries GROUP BY accountId", nativeQuery = true)  /* (4,5) Revenue,Cost and Expenses*/
    List<Object[]> getLedgerEntriesByAsOfDate(@Param("asOfDate") String asOfDate);

    @Query(value = "SELECT " +
            "debit, " +
            "credit, " +
            "Account.id, " +
            "Account.code, " +
            "Account.title, " +
            "Account.hasSL " +
            "FROM " +
            "( " +
            "  SELECT " +
            "  SegmentAccount.FK_accountId as accountId, " +
            "  SUM(COALESCE(GeneralLedger.debit,0)) AS debit, " +
            "  SUM(COALESCE(GeneralLedger.credit,0)) AS credit, " +
            "  IF(COALESCE(GeneralLedger.debit,0) = 0, 1, 0) as side " +
            "  FROM GeneralLedger " +
            "  JOIN AdjustmentJournal ON GeneralLedger.FK_transactionId = AdjustmentJournal.FK_transactionId " +
            "  JOIN SegmentAccount ON GeneralLedger.FK_segmentAccountId = SegmentAccount.id " +
            "  WHERE AdjustmentJournal.id = (SELECT id FROM AdjustmentJournal aj WHERE aj.transactionType = 'CLOSING' AND aj.`year` = :year ORDER BY aj.voucherDate DESC LIMIT 1) " +
            "  GROUP BY SegmentAccount.FK_accountId " +
            "  ORDER BY side, SegmentAccount.accountCode " +
            "  ) as gl " +
            "JOIN Account ON gl.accountId = Account.id", nativeQuery = true)
    List<Object[]> getLedgerEntriesClosing(@Param("year") Integer year);

    AdjustmentJournal findFirstByOrderByIdDesc();
}
