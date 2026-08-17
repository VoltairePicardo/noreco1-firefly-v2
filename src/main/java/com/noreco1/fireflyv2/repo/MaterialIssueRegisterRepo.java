package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.MaterialIssueRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by nsutgio2015 on 4/27/2015.
 */
public interface MaterialIssueRegisterRepo extends JpaRepository<MaterialIssueRegister, Integer> {
    @Query(value = "SELECT e.code FROM MaterialIssueRegister e WHERE year = :year  AND code LIKE '%MIV%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestMaterialIssueRegisterCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT "
            + " "
            + "	MaterialIssueRegister.id, "
            + "	MaterialIssueRegister.code, "
            + "	MaterialIssueRegister.particulars, "
            + "	MaterialIssueRegister.voucherDate, "
            + "	MaterialIssueRegister.FK_transactionId, "
            + "	MaterialIssueRegister.inventoryDocType, "
            + " CASE MaterialIssueRegister.inventoryDocType"
            + " WHEN 'MCT' THEN (select code from StockRelease where StockRelease.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'SA' THEN (select code from StockAdjustment where StockAdjustment.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'STR' THEN (select code from StockReceive where StockReceive.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MCRT' THEN (select code from MaterialCreditTicket where MaterialCreditTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MST' THEN (select code from MaterialsalvageTicket where MaterialsalvageTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " ELSE '' "
            + " END as documentNo "
            + "	 "
            + "FROM MaterialIssueRegister "
            + "	WHERE MaterialIssueRegister.voucherDate >= :from "
            + "		AND MaterialIssueRegister.voucherDate <= :to "
            + "		AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId "
            + "	ORDER BY voucherDate, code", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT "
            + " "
            + "	MaterialIssueRegister.id, "
            + "	MaterialIssueRegister.code, "
            + "	MaterialIssueRegister.particulars, "
            + "	MaterialIssueRegister.voucherDate, "
            + "	MaterialIssueRegister.FK_transactionId, "
            + "	MaterialIssueRegister.inventoryDocType, "
            + " CASE MaterialIssueRegister.inventoryDocType"
            + " WHEN 'MCT' THEN (select code from StockRelease where StockRelease.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'SA' THEN (select code from StockAdjustment where StockAdjustment.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'STR' THEN (select code from StockReceive where StockReceive.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MCRT' THEN (select code from MaterialCreditTicket where MaterialCreditTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MST' THEN (select code from MaterialsalvageTicket where MaterialsalvageTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " ELSE '' "
            + " END as documentNo "
            + "	 "
            + "FROM MaterialIssueRegister "
            + "	WHERE MaterialIssueRegister.voucherDate >= :from "
            + "		AND MaterialIssueRegister.voucherDate <= :to "
            + "		AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId"
            + "     AND MaterialIssueRegister.FK_officeId = :officeId "
            + "	ORDER BY voucherDate, code", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("officeId") Integer officeId);

    @Query(value = "SELECT " +
            "SUM(generals.glDebit) AS glDebit, " +
            "SUM(generals.glCredit) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, " +
            "if (glDebit-glCredit > 0 , 1, 2) as side,  " +
            "if (subs.id IS NOT null , 1, 0) as isSl "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, " +
            "SUM(GeneralLedger.debit) AS glDebit, " +
            "SUM(GeneralLedger.credit) AS glCredit " +
            "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = 7 "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, " +
            "SUM(SubLedger.debit) AS slDebit, " +
            "SUM(SubLedger.credit) AS slCredit, " +
            "SubLedger.id " +
            "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, "
            + "	if (glDebit-glCredit > 0 , 1, 2) as side "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit "
            + "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit "
            + "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRange(@Param("from") String from, @Param("to") String to);

    @Query(value = "SELECT " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, "
            + "	if (glDebit-glCredit > 0 , 1, 2) as side "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit "
            + "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = 7 AND MaterialIssueRegister.FK_officeId = :officeId "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit "
            + "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId AND MaterialIssueRegister.FK_officeId = :officeId "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to, @Param("officeId") Integer officeId,  @Param("documentStatusId") Integer documentStatusId);

    public MaterialIssueRegister findOneByTransactionId(Integer transId);
    public MaterialIssueRegister findOneByTransactionIdAndInventoryDocType(Integer transId, String inventoryDocType);

    public List<MaterialIssueRegister> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    public List<MaterialIssueRegister> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    public List<MaterialIssueRegister> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    MaterialIssueRegister findFirstByOrderByIdAsc();

    @Query(value = "SELECT doc.* " +
            "FROM MaterialIssueRegister doc " +
            "LEFT JOIN MonthlyCycle mc " +
            "    ON YEAR(doc.voucherDate) = mc.year " +
            "    AND MONTH(doc.voucherDate) = mc.month " +
            "WHERE doc.voucherDate BETWEEN :startDate AND :endDate " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusIds) " +
            "AND (mc.status IS NULL OR mc.status = 'OPEN') ", nativeQuery = true)
    public List<MaterialIssueRegister> findByVoucherDateBetweenAndDocumentStatusIdNotInForCancellation(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    public List<MaterialIssueRegister> findByVoucherDateBetweenAndDocumentStatusIdNotInAndInventoryDocType(Date from, Date to, Collection<Integer> documentStatusIds, String inventoryDocType);

    public List<MaterialIssueRegister> findByDocumentStatusIdAndInventoryDocTypeAndVoucherDateBetweenAndOfficeId(Integer statusId, String inventoryDocType, Date from, Date to, Integer officeId);
    public List<MaterialIssueRegister> findByDocumentStatusIdAndInventoryDocTypeAndVoucherDateBetween(Integer statusId, String inventoryDocType, Date from, Date to);

    @Query(value = "SELECT "
            + " "
            + "	MaterialIssueRegister.id, "
            + "	MaterialIssueRegister.code, "
            + "	MaterialIssueRegister.particulars, "
            + "	MaterialIssueRegister.voucherDate, "
            + "	MaterialIssueRegister.FK_transactionId, "
            + "	MaterialIssueRegister.inventoryDocType, "
            + " CASE MaterialIssueRegister.inventoryDocType"
            + " WHEN 'MCT' THEN (select code from StockRelease where StockRelease.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'SA' THEN (select code from StockAdjustment where StockAdjustment.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'STR' THEN (select code from StockReceive where StockReceive.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MCRT' THEN (select code from MaterialCreditTicket where MaterialCreditTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MST' THEN (select code from MaterialsalvageTicket where MaterialsalvageTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " ELSE '' "
            + " END as documentNo "
            + "	 "
            + "FROM MaterialIssueRegister "
            + "	WHERE MaterialIssueRegister.voucherDate >= :from "
            + "		AND MaterialIssueRegister.voucherDate <= :to "
            + "		AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId "
            + "		AND MaterialIssueRegister.inventoryDocType = :inventoryDocType "
            + "	ORDER BY voucherDate, code", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRange(@Param("from") String from, @Param("to") String to,  @Param("documentStatusId") Integer documentStatusId, @Param("inventoryDocType")  String inventoryDocType);

    @Query(value = "SELECT "
            + " "
            + "	MaterialIssueRegister.id, "
            + "	MaterialIssueRegister.code, "
            + "	MaterialIssueRegister.particulars, "
            + "	MaterialIssueRegister.voucherDate, "
            + "	MaterialIssueRegister.FK_transactionId, "
            + "	MaterialIssueRegister.inventoryDocType, "
            + " CASE MaterialIssueRegister.inventoryDocType"
            + " WHEN 'MCT' THEN (select code from StockRelease where StockRelease.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'SA' THEN (select code from StockAdjustment where StockAdjustment.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'STR' THEN (select code from StockReceive where StockReceive.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MCRT' THEN (select code from MaterialCreditTicket where MaterialCreditTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " WHEN 'MST' THEN (select code from MaterialsalvageTicket where MaterialsalvageTicket.FK_transactionId = MaterialIssueRegister.invDocTransactionId) "
            + " ELSE '' "
            + " END as documentNo "
            + "	 "
            + "FROM MaterialIssueRegister "
            + "	WHERE MaterialIssueRegister.voucherDate >= :from "
            + "		AND MaterialIssueRegister.voucherDate <= :to "
            + "		AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId "
            + "		AND MaterialIssueRegister.inventoryDocType = :inventoryDocType "
            + "		AND MaterialIssueRegister.FK_officeId = :officeId "
            + "	ORDER BY voucherDate, code", nativeQuery = true)
    public List<Object[]> findForRegisterByDateRangeAndOfficeId(@Param("from") String from, @Param("to") String to, @Param("officeId") Integer officeId,  @Param("documentStatusId") Integer documentStatusId, @Param("inventoryDocType")  String inventoryDocType);

    @Query(value = "SELECT " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, "
            + "	if (glDebit-glCredit > 0 , 1, 2) as side "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit "
            + "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = 7 "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit "
            + "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndInvDocType(@Param("from") String from, @Param("to") String to,
                                                                       @Param("documentStatusId") Integer documentStatusId,
                                                                       @Param("invDocumentType") String invDocumentType
                                                                       );

    @Query(value = "SELECT " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, "
            + "	if (glDebit-glCredit > 0 , 1, 2) as side "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit "
            + "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit "
            + "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndInvDocType(@Param("from") String from, @Param("to") String to,
                                                                       @Param("invDocumentType") String invDocumentType
    );

    @Query(value = "SELECT " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)), 0) AS glDebit, " +
            "IF(SUM(COALESCE(generals.glDebit, 0))-SUM(COALESCE(generals.glCredit, 0)) > 0, 0, SUM(COALESCE(generals.glCredit, 0))-SUM(COALESCE(generals.glDebit, 0))) AS glCredit, " +
            "generals.glId,  " +
            "SUM(COALESCE(subs.slDebit, 0)) slDebit,  " +
            "SUM(COALESCE(subs.slCredit, 0)) slCredit,  "
            + "	subs.accountNo, "
            + "	SegmentAccount.accountCode, "
            + "	Account.title, "
            + "	slentity.name, "
            + "	if (glDebit-glCredit > 0 , 1, 2) as side "
            + " "
            + "FROM (SELECT "
            + "			GeneralLedger.id as glId, "
            + "			GeneralLedger.FK_segmentAccountId as glSegmentAccountId, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)), 0) AS glDebit, "
            + "			IF(SUM(COALESCE(GeneralLedger.debit, 0))-SUM(COALESCE(GeneralLedger.credit, 0)) > 0, 0, SUM(COALESCE(GeneralLedger.credit, 0))-SUM(COALESCE(GeneralLedger.debit, 0))) AS glCredit "
            + "		FROM GeneralLedger "
            + "			JOIN MaterialIssueRegister on GeneralLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "		WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = 7 AND MaterialIssueRegister.FK_officeId = :officeId "
            + "		GROUP BY GeneralLedger.FK_segmentAccountId) as generals "
            + "			LEFT JOIN(SELECT "
            + "							SubLedger.FK_generalLedgerLineId AS slGLId, "
            + "							SubLedger.FK_segmentAccountId as slSegmentAccountId, "
            + "							SubLedger.FK_accountNo as accountNo, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)), 0) AS slDebit, "
            + "							IF(SUM(COALESCE(SubLedger.debit, 0))-SUM(COALESCE(SubLedger.credit, 0)) > 0, 0, SUM(COALESCE(SubLedger.credit, 0))-SUM(COALESCE(SubLedger.debit, 0))) AS slCredit "
            + "						FROM SubLedger "
            + "							JOIN MaterialIssueRegister on SubLedger.FK_transactionId = MaterialIssueRegister.FK_transactionId AND MaterialIssueRegister.inventoryDocType = :invDocumentType "
            + "						WHERE (MaterialIssueRegister.voucherDate >= :from AND MaterialIssueRegister.voucherDate <= :to) AND MaterialIssueRegister.FK_documentStatusId = :documentStatusId AND MaterialIssueRegister.FK_officeId = :officeId "
            + "						GROUP BY SubLedger.FK_segmentAccountId, SubLedger.FK_accountNo) as subs "
            + "			ON generals.glSegmentAccountId = subs.slSegmentAccountId "
            + "			JOIN SegmentAccount ON generals.glSegmentAccountId = SegmentAccount.id "
            + "			JOIN Account ON SegmentAccount.FK_accountId = Account.id "
            + "			JOIN BusinessSegment ON SegmentAccount.FK_businessSegmentId  = BusinessSegment.id "
            + "			LEFT JOIN slentity ON  subs.accountNo = slentity.accountNo " +
            "GROUP BY Account.code, subs.accountNo "
            + "ORDER BY side, Account.code, BusinessSegment.code", nativeQuery = true)
    public List<Object[]> findForRegisterRecapByDateRangeAndInvDocTypeAndOfficeId(@Param("from") String from, @Param("to") String to, @Param("officeId") Integer officeId,
                                                                       @Param("documentStatusId") Integer documentStatusId,
                                                                       @Param("invDocumentType") String invDocumentType
    );

    List<MaterialIssueRegister> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<MaterialIssueRegister> findByDocumentStatusId(Integer status);
    MaterialIssueRegister findFirstByOrderByIdDesc();
}
