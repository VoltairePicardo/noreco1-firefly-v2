package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CashReceipts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CashReceiptsRepo extends JpaRepository<CashReceipts, Integer> {
    @Query(value = "SELECT e.code FROM CashReceipts e WHERE year = :year AND code LIKE '%CRV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCrCodeByYear(@Param("year") Integer year);
    public List<CashReceipts> findByDocumentStatusId(Integer statusId);

    @Query(value = "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "CashReceipts.FK_transactionId " +
            "FROM CashReceipts " +
            "WHERE CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to " +
            "AND CashReceipts.FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "CashReceipts.FK_transactionId " +
            "FROM CashReceipts " +
            "WHERE CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "CashReceipts.id, " +
            "CashReceipts.code, " +
            "CashReceipts.particulars, " +
            "CashReceipts.voucherDate, " +
            "CashReceipts.FK_transactionId " +
            "FROM CashReceipts " +
            "WHERE CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to " +
            "AND CashReceipts.FK_documentStatusId = :documentStatusId " +
            "AND CashReceipts.FK_officeId = :officeId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId,  @Param("officeId") Integer officeId);

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
            "JOIN CashReceipts on GeneralLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) AND CashReceipts.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN CashReceipts on SubLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) AND CashReceipts.FK_documentStatusId = :documentStatusId " +
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
            "JOIN CashReceipts on GeneralLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN CashReceipts on SubLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) " +
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
            "if (glDebit-glCredit > 0 , 1, 2) as side, " +
            "if (subs.id IS NOT null , 1, 0) as isSl " +
            "FROM (SELECT " +
            "GeneralLedger.id as glId," +
            "GeneralLedger.FK_segmentAccountId as glSegmentAccountId," +
            "IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit," +
            "IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit  " +
            "FROM GeneralLedger " +
            "JOIN CashReceipts on GeneralLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) AND CashReceipts.FK_documentStatusId = :documentStatusId AND CashReceipts.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, " +
            "IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit  " +
            "FROM SubLedger " +
            "JOIN CashReceipts on SubLedger.FK_transactionId = CashReceipts.FK_transactionId " +
            "WHERE (CashReceipts.voucherDate >= :from AND CashReceipts.voucherDate <= :to) AND CashReceipts.FK_documentStatusId = :documentStatusId AND CashReceipts.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId,  @Param("officeId") Integer officeId);

    public CashReceipts findOneByTransactionId(Integer transId);

    public List<CashReceipts> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    public List<CashReceipts> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    public List<CashReceipts> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    public List<CashReceipts> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    CashReceipts findFirstByOrderByIdAsc();

    @Query(value = "SELECT doc.* " +
            "FROM CashReceipts doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<CashReceipts> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    List<CashReceipts> findByDocumentStatusIdNotIn(List<Integer> integers);
    CashReceipts findFirstByOrderByIdDesc();

    @Query(value = "SELECT " +
            "SegmentAccount.accountCode,  " +
            "Account.title,  " +
            "(COALESCE(SUM(GeneralLedger.debit), 0)) as glDebit,  " +
            "(COALESCE(SUM(GeneralLedger.credit), 0)) as glCredit, " +
            "GeneralLedger.id as glId " +
            "from GeneralLedger  " +
            "JOIN CashReceipts ON GeneralLedger.FK_transactionId = CashReceipts.FK_transactionId  " +
            "JOIN SegmentAccount SegmentAccount on GeneralLedger.FK_segmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "WHERE CashReceipts.voucherDate BETWEEN :from AND :to AND CashReceipts.FK_documentStatusId = :documentStatusId " +
            "GROUP BY Account.code " +
            "ORDER BY Account.code ",  nativeQuery = true)
    List<Object[]> findForCrvRecapGL(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "SegmentAccount.accountCode,  " +
            "Account.title,  " +
            "(COALESCE(SUM(GeneralLedger.debit), 0)) as glDebit,  " +
            "(COALESCE(SUM(GeneralLedger.credit), 0)) as glCredit, " +
            "GeneralLedger.id as glId " +
            "from GeneralLedger  " +
            "JOIN CashReceipts ON GeneralLedger.FK_transactionId = CashReceipts.FK_transactionId  " +
            "JOIN SegmentAccount SegmentAccount on GeneralLedger.FK_segmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id " +
            "WHERE CashReceipts.voucherDate BETWEEN :from AND :to " +
            "GROUP BY Account.code " +
            "ORDER BY Account.code ",  nativeQuery = true)
    List<Object[]> findForCrvRecapGL(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT  " +
            "SubLedger.id as slId,   " +
            "SubLedger.FK_generalLedgerLineId as glId,  " +
            "slentity.accountNo AS accountNumber,  " +
            "slentity.name AS accountName,     " +
            "(COALESCE(SubLedger.debit, 0)) as glDebit,   " +
            "(COALESCE(SubLedger.credit, 0)) as glCredit " +
            "from SubLedger   " +
            "JOIN CashReceipts ON SubLedger.FK_transactionId = CashReceipts.FK_transactionId   " +
            "JOIN SegmentAccount SegmentAccount on SubLedger.FK_segmentAccountId = SegmentAccount.id   " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "LEFT JOIN slentity ON  SubLedger.FK_accountNo = slentity.accountNo  " +
            "WHERE SubLedger.FK_generalLedgerLineId = :glId  " +
            "ORDER BY Account.code ",  nativeQuery = true)
    List<Object[]> findForCrvRecapSL(@Param("glId") Integer glId);

}
