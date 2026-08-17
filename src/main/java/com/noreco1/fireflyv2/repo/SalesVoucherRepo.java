package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.SalesVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface SalesVoucherRepo extends JpaRepository<SalesVoucher, Integer> {
    @Query(value = "SELECT e.code FROM SalesVoucher e WHERE year = :year  AND code LIKE '%SV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestSvCodeByYear(@Param("year") Integer year);
    public List<SalesVoucher> findByDocumentStatusId(Integer statusId);

    @Query(value = "SELECT " +
            "SalesVoucher.id, " +
            "SalesVoucher.code, " +
            "SalesVoucher.particulars, " +
            "SalesVoucher.voucherDate, " +
            "SalesVoucher.FK_transactionId " +
            "FROM SalesVoucher " +
            "WHERE SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to " +
            "AND SalesVoucher.FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "SalesVoucher.id, " +
            "SalesVoucher.code, " +
            "SalesVoucher.particulars, " +
            "SalesVoucher.voucherDate, " +
            "SalesVoucher.FK_transactionId " +
            "FROM SalesVoucher " +
            "WHERE SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "SalesVoucher.id, " +
            "SalesVoucher.code, " +
            "SalesVoucher.particulars, " +
            "SalesVoucher.voucherDate, " +
            "SalesVoucher.FK_transactionId " +
            "FROM SalesVoucher " +
            "WHERE SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to " +
            "AND SalesVoucher.FK_documentStatusId = :documentStatusId " +
            "AND SalesVoucher.FK_officeId = :officeId " +
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
            "JOIN SalesVoucher on GeneralLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) AND SalesVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN SalesVoucher on SubLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) AND SalesVoucher.FK_documentStatusId = :documentStatusId " +
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
            "JOIN SalesVoucher on GeneralLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN SalesVoucher on SubLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) " +
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
            "JOIN SalesVoucher on GeneralLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) AND SalesVoucher.FK_documentStatusId = :documentStatusId AND SalesVoucher.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN SalesVoucher on SubLedger.FK_transactionId = SalesVoucher.FK_transactionId " +
            "WHERE (SalesVoucher.voucherDate >= :from AND SalesVoucher.voucherDate <= :to) AND SalesVoucher.FK_documentStatusId = :documentStatusId AND SalesVoucher.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId,  @Param("officeId") Integer officeId);

    public SalesVoucher findOneByTransactionId(Integer transId);

    List<SalesVoucher> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<SalesVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<SalesVoucher> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    List<SalesVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    SalesVoucher findFirstByOrderByIdAsc();

    @Query(value = "SELECT doc.* " +
            "FROM SalesVoucher doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<SalesVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    List<SalesVoucher> findByDocumentStatusIdNotIn(List<Integer> integers);
    SalesVoucher findFirstByOrderByIdDesc();
}
