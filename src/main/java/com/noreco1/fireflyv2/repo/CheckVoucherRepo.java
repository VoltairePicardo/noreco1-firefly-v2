package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.CheckVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CheckVoucherRepo extends JpaRepository<CheckVoucher, Integer> {
    @Query(value = "SELECT e.code FROM CheckVoucher e WHERE year = :year  AND code LIKE '%CV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestCvCodeByYear(@Param("year") Integer year);

    public CheckVoucher findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.FK_transactionId, " +
            "CheckVoucher.checkAmount, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.code, " +
            "CheckVoucher.voucherDate, " +
            "slentity.name, " +
            "CheckVoucher.remarks, " +
            "CheckVoucherCheque.id AS `cvcId` " +
            "FROM CheckVoucher " +
            "JOIN CheckVoucherCheque ON CheckVoucher.FK_transactionId = CheckVoucherCheque.FK_transactionId " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE CheckVoucher.FK_documentStatusId = 7 " +
            "AND CheckVoucherCheque.released = 0", nativeQuery = true)
    public List<Object[]> findAllForCheckReleasing();

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "CheckVoucher.FK_transactionId, " +
            "slentity.name " +
            "FROM CheckVoucher " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to " +
            "AND CheckVoucher.FK_documentStatusId = :documentStatusId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "CheckVoucher.FK_transactionId, " +
            "slentity.name " +
            "FROM CheckVoucher " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucher.code, " +
            "CheckVoucher.particulars, " +
            "CheckVoucher.voucherDate, " +
            "CheckVoucher.FK_transactionId, " +
            "slentity.name " +
            "FROM CheckVoucher " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to " +
            "AND CheckVoucher.FK_documentStatusId = :documentStatusId " +
            "AND CheckVoucher.FK_officeId = :officeId " +
            "ORDER BY voucherDate ASC, code ASC", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAngOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

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
            "JOIN CheckVoucher on GeneralLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) AND CheckVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN CheckVoucher on SubLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) AND CheckVoucher.FK_documentStatusId = :documentStatusId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRange(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

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
            "JOIN CheckVoucher on GeneralLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN CheckVoucher on SubLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) " +
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
            "JOIN CheckVoucher on GeneralLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) AND CheckVoucher.FK_documentStatusId = :documentStatusId AND CheckVoucher.FK_officeId = :officeId " +
            "GROUP BY GeneralLedger.FK_segmentAccountId) as generals  " +
            "LEFT JOIN (SELECT " +
            "SubLedger.FK_generalLedgerLineId AS slGLId, " +
            "SubLedger.FK_segmentAccountId as slSegmentAccountId," +
            "SubLedger.FK_accountNo as accountNo," +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "FROM SubLedger " +
            "JOIN CheckVoucher on SubLedger.FK_transactionId = CheckVoucher.FK_transactionId " +
            "WHERE (CheckVoucher.voucherDate >= :from AND CheckVoucher.voucherDate <= :to) AND CheckVoucher.FK_documentStatusId = :documentStatusId AND CheckVoucher.FK_officeId = :officeId " +
            "GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs  " +
            "ON generals.glSegmentAccountId = subs.slSegmentAccountId  " +
            "JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id  " +
            "JOIN Account ON SegmentAccount.FK_accountId = Account.id  " +
            "JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id  " +
            "LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo " +
            "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

    List<CheckVoucher> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<CheckVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(Date from, Date to, Collection<Integer> documentStatusIds);
    List<CheckVoucher> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    List<CheckVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeIdOrderByCode(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    CheckVoucher findFirstByOrderByIdAsc();

    @Query(value = "SELECT doc.* " +
            "FROM CheckVoucher doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<CheckVoucher> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "SELECT " +
            "cv.id, " +
            "cv.FK_transactionId, " +
            "cv.checkAmount, " +
            "cv.particulars, " +
            "cv.code, " +
            "cv.voucherDate, " +
            "slentity.name, " +
            "cv.FK_payeeAccountNo " +
            "FROM CheckVoucher cv " +
            "JOIN slentity ON cv.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE cv.FK_transactionId NOT IN " +
            "(SELECT " +
            "r.FK_checkVoucherTransactionId " +
            "FROM Replenishment r) " +
            "AND cv.FK_documentStatusId = :documentStatusId " +
            "AND cv.voucherDate BETWEEN :from AND :to " +
            "GROUP BY cv.id", nativeQuery = true)
    List<Object[]> findByDocumentStatusIdAndTransactionIdInAndVoucherDateBetween(@Param("from") Date from,
                                                                                 @Param("to") Date to,
                                                                                 @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "s.name, " +
            "s.tin, " +
            "cvip.baseAmount, " +
            "cvip.percentage, " +
            "cvip.amount " +
            "FROM CheckVoucher cv " +
            "INNER JOIN CheckVoucherIncomePayment cvip ON cvip.FK_transactionId = cv.FK_transactionId " +
            "INNER JOIN Supplier s ON s.FK_accountNo = cv.FK_payeeAccountNo " +
            "WHERE YEAR(cv.voucherDate) = :year AND MONTH(cv.voucherDate) = :month " +
            "AND cv.FK_documentStatusId = :docStatId " +
            "ORDER BY s.name", nativeQuery = true)
    List<Object[]> findCheckVoucherIncomePayment(@Param("year") Integer year, @Param("month")Integer month, @Param("docStatId")Integer docStatId);

    List<CheckVoucher> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<CheckVoucher> findByDocumentStatusId(Integer status);
    CheckVoucher findFirstByOrderByIdDesc();

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucherReleasedCheque.FK_checkVoucherChequeId, " +
            "CheckVoucher.code, " +
            "CheckVoucher.voucherDate, " +
            "CheckVoucher.checkAmount, " +
            "CheckVoucherCheque.checkNumber, " +
            "CheckVoucherReleasedCheque.dateReleased, " +
            "User.fullName, " +
            "slentity.name, " +
            "CASE " +
            "    WHEN CheckVoucherCheque.released THEN 'Released' ELSE 'Cancelled' " +
            "END AS `status` " +
            "FROM CheckVoucherReleasedCheque " +
            "JOIN CheckVoucherCheque ON CheckVoucherCheque.id = CheckVoucherReleasedCheque.FK_checkVoucherChequeId " +
            "JOIN CheckVoucher ON CheckVoucher.FK_transactionId = CheckVoucherCheque.FK_transactionId " +
            "JOIN User ON User.id = CheckVoucherReleasedCheque.FK_createdByUserId " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo ", nativeQuery = true)
    public List<Object[]> findAllForReleasedCheck();

    @Query(value = "SELECT " +
            "CheckVoucher.id, " +
            "CheckVoucherReleasedCheque.FK_checkVoucherChequeId, " +
            "CheckVoucher.code, " +
            "CheckVoucher.voucherDate, " +
            "CheckVoucher.checkAmount, " +
            "CheckVoucherCheque.checkNumber, " +
            "CheckVoucherReleasedCheque.dateReleased, " +
            "User.fullName, " +
            "slentity.name, " +
            "CASE " +
            "    WHEN CheckVoucherCheque.released THEN 'Released' ELSE 'Cancelled' " +
            "END AS `status` " +
            "FROM CheckVoucherReleasedCheque " +
            "JOIN CheckVoucherCheque ON CheckVoucherCheque.id = CheckVoucherReleasedCheque.FK_checkVoucherChequeId " +
            "JOIN CheckVoucher ON CheckVoucher.FK_transactionId = CheckVoucherCheque.FK_transactionId " +
            "JOIN User ON User.id = CheckVoucherReleasedCheque.FK_createdByUserId " +
            "JOIN slentity ON CheckVoucher.FK_payeeAccountNo = slentity.accountNo " +
            "WHERE slentity.name LIKE :searchText ", nativeQuery = true)
    public List<Object[]> findAllForReleasedCheckBySupplier(@Param("searchText") String searchText);

    Page<CheckVoucher> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}
