package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashAdvance;
import com.noreco1.fireflyv2.model.JournalVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface JournalVoucherRepo extends JpaRepository<JournalVoucher, Integer> {
    @Query(value = "SELECT e.code FROM JournalVoucher e WHERE year = :year AND code LIKE '%JV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestVvCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "JournalVoucher.FK_transactionId," +
            "JournalVoucher.remarks " +
            "FROM JournalVoucher " +
            "WHERE JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to " +
            "AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "JournalVoucher.FK_transactionId," +
            "JournalVoucher.remarks " +
            "FROM JournalVoucher " +
            "WHERE JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "JournalVoucher.id, " +
            "JournalVoucher.code, " +
            "JournalVoucher.explanation, " +
            "JournalVoucher.voucherDate, " +
            "JournalVoucher.FK_transactionId," +
            "JournalVoucher.remarks " +
            "FROM JournalVoucher " +
            "WHERE JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to " +
            "AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
            "AND JournalVoucher.FK_officeId = :officeId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

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
            "JOIN JournalVoucher on GeneralLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, " +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit  " +
            "FROM SubLedger " +
            "JOIN JournalVoucher on SubLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
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
            "JOIN JournalVoucher on GeneralLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId AND JournalVoucher.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN JournalVoucher on SubLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId AND JournalVoucher.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

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
            "JOIN JournalVoucher on GeneralLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN JournalVoucher on SubLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) AND JournalVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

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
            "JOIN JournalVoucher on GeneralLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN JournalVoucher on SubLedger.FK_transactionId = JournalVoucher.FK_transactionId " +
            "WHERE (JournalVoucher.voucherDate >= :from AND JournalVoucher.voucherDate <= :to) " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to);

    public JournalVoucher findOneByTransactionId(Integer transId);

    public List<JournalVoucher> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    public List<JournalVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    public List<JournalVoucher> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    public List<JournalVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    JournalVoucher findFirstByOrderByIdAsc();
    JournalVoucher findFirstByOrderByIdDesc();

    @Query(value = "SELECT doc.* " +
            "FROM JournalVoucher doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<JournalVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    List<JournalVoucher> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<JournalVoucher> findByDocumentStatusId(Integer status);

    @Query(value = "SELECT * FROM JournalVoucher jv " +
            "WHERE jv.FK_documentStatusId = :documentStatusId " +
            "AND jv.payable IS TRUE " +
            "AND jv.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherJv.FK_journalVoucherId  " +
            "  FROM CheckVoucherJv " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJv.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM JournalVoucher jv " +
                    "WHERE jv.FK_documentStatusId = :documentStatusId " +
                    "AND jv.payable IS TRUE " +
                    "AND jv.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherJv.FK_journalVoucherId  " +
                    "  FROM CheckVoucherJv " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJv.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") ",
            nativeQuery = true)
    Page<JournalVoucher> findAllByDocumentStatusForCv(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT * FROM JournalVoucher jv " +
            "WHERE jv.FK_documentStatusId = :documentStatusId " +
            "AND jv.payable IS TRUE " +
            "AND jv.code LIKE :query " +
            "AND jv.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherJv.FK_journalVoucherId  " +
            "  FROM CheckVoucherJv " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJv.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "\n#pageable\n",
            countQuery = "SELECT * FROM JournalVoucher jv " +
                    "WHERE jv.FK_documentStatusId = :documentStatusId " +
                    "AND jv.payable IS TRUE " +
                    "AND jv.code LIKE :query " +
                    "AND jv.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherJv.FK_journalVoucherId  " +
                    "  FROM CheckVoucherJv " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherJv.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") ",
            nativeQuery = true)
    Page<JournalVoucher> findAllByQueryAndDocumentStatusForCv(@Param("query") String query,
                                                              @Param("documentStatusId") Integer documentStatusId,
                                                              Pageable pageable);

    Page<JournalVoucher> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}