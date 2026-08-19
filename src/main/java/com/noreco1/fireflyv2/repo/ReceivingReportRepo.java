package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.ReceivingReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ReceivingReportRepo extends JpaRepository<ReceivingReport, Integer> {

    @Query(value = "SELECT e.code FROM ReceivingReport e WHERE year = :year  AND code LIKE '%RR%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestRRCodeByYear(@Param("year") Integer year);

    ReceivingReport findFirstByOrderByIdAsc();
    List<ReceivingReport> findByDocumentStatusIdAndDeliveryDateBetween(Integer statusId, Date from, Date to);
    List<ReceivingReport> findByDeliveryDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<ReceivingReport> findByDocumentStatusIdAndDeliveryDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    List<ReceivingReport> findByDeliveryDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    ReceivingReport findOneByTransactionId(Integer transId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "DISTINCT ReceivingReport.id, " +
            "ReceivingReport.code as code, " +
            "ReceivingReport.totalAmount, " +
            "ReceivingReport.invoiceDescription as particulars, " +
            "ReceivingReport.deliveryDate, " +
            "u.fullName as preparedBy, " +
            "Supplier.FK_accountNo as accountNo, " +
            "Supplier.name as slentityName " +
            "FROM ReceivingReport " +
            "JOIN User u ON ReceivingReport.FK_createdByUserId = u.id  " +
            "JOIN Supplier ON ReceivingReport.FK_supplierId = Supplier.id " +
            "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId " +
            "AND ReceivingReport.id NOT IN (SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a " +
            "INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id " +
            "WHERE apv.FK_documentStatusId = :documentStatusId AND a.FK_documentTypeId = :docTypeId)", nativeQuery = true)
    public List<Object[]> findAllForApv(@Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "rr.id, " +
            "cv.code AS cvCode, " +
            "cv.amount AS cvAmount, " +
            "cal.id AS calId, " +
            "cal.code AS calCode, " +
            "cal.amount, " +
            "ca.code AS caCode, " +
            "po.useCreditCard " +
            "FROM ReceivingReport rr " +
            "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
            "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
            "JOIN PurchaseOrder po ON pod.FK_purchaseOrderId = po.id " +
            "LEFT JOIN CashAdvance ca ON po.FK_cashAdvanceId = ca.id " +
            "LEFT JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId " +
            "LEFT JOIN CheckVoucher cv ON po.id = cv.FK_purchaseOrderId " +
            "WHERE rr.id = :rrId " +
            "GROUP BY rr.id, cv.code, cv.amount, cal.id, cal.code, cal.amount, ca.code, po.useCreditCard", nativeQuery = true)
    public List<Object[]> detailsForAccountSetting(@Param("rrId") Integer rrId);

    @Transactional(readOnly = true)
    @Query(value = "SELECT  " +
            "ReceivingReport.id, " +
            "ReceivingReport.code as code, " +
            "ReceivingReport.totalAmount, " +
            "ReceivingReport.invoiceNumber as particulars, " +
            "ReceivingReport.deliveryDate, " +
            "u.fullName as preparedBy " +
            "FROM ReceivingReport " +
            "JOIN User u ON ReceivingReport.FK_createdByUserId = u.id  " +
            "JOIN AccountsPayableVoucherLink ON ReceivingReport.id = AccountsPayableVoucherLink.FK_linkedDocumentId " +
            "WHERE AccountsPayableVoucherLink.FK_accountsPayableVoucherId = :apvId " +
            "AND AccountsPayableVoucherLink.FK_documentTypeId = :docTypeId LIMIT 1", nativeQuery = true)
    List<Object[]> findByApvId(@Param("apvId") Integer apvId, @Param("docTypeId") Integer docTypeId);

    @Query(value = "SELECT " +
            "rr.* " +
            "FROM ReceivingReport rr " +
            "JOIN Supplier s ON rr.FK_supplierId = s.id  " +
            "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
            "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
            "JOIN PurchaseOrder po ON pod.FK_purchaseOrderId = po.id " +
            "LEFT JOIN CashAdvance ca ON po.FK_cashAdvanceId = ca.id " +
            "LEFT JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId AND cal.FK_documentStatusId = 7 " +
            "LEFT JOIN CheckVoucher cv ON po.id = cv.FK_purchaseOrderId AND cal.FK_documentStatusId = 7 " +
            "WHERE rr.FK_documentStatusId = 7 " +
            "AND (UPPER(rr.code) LIKE :query OR UPPER(s.name) LIKE :query) " +
            "AND (rr.id NOT IN (SELECT COALESCE(AccountSetting.FK_receivingReportId, 0) FROM accountsetting) " +
            "OR cal.id IS NOT NULL " +
            "OR cv.id IS NOT NULL " +
            "OR po.useCreditCard) " +
            "GROUP BY rr.id " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(DISTINCT rr.id)  " +
                    "FROM ReceivingReport rr " +
                    "JOIN Supplier s ON rr.FK_supplierId = s.id  " +
                    "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
                    "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
                    "JOIN PurchaseOrder po ON pod.FK_purchaseOrderId = po.id " +
                    "LEFT JOIN CashAdvance ca ON po.FK_cashAdvanceId = ca.id " +
                    "LEFT JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId AND cal.FK_documentStatusId = 7 " +
                    "LEFT JOIN CheckVoucher cv ON po.id = cv.FK_purchaseOrderId AND cal.FK_documentStatusId = 7 " +
                    "WHERE rr.FK_documentStatusId = 7 " +
                    "AND (UPPER(rr.code) LIKE :query OR UPPER(s.name) LIKE :query) " +
                    "AND (rr.id NOT IN (SELECT COALESCE(AccountSetting.FK_receivingReportId, 0) FROM accountsetting) " +
                    "OR cal.id IS NOT NULL " +
                    "OR cv.id IS NOT NULL " +
                    "OR po.useCreditCard)",
            nativeQuery = true)
    Page<ReceivingReport> findAllByQueryForApv(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT " +
            "rr.* " +
            "FROM ReceivingReport rr " +
            "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
            "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
            "JOIN PurchaseOrder po ON pod.FK_purchaseOrderId = po.id " +
            "LEFT JOIN CashAdvance ca ON po.FK_cashAdvanceId = ca.id " +
            "LEFT JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId AND cal.FK_documentStatusId = 7 " +
            "LEFT JOIN CheckVoucher cv ON po.id = cv.FK_purchaseOrderId AND cal.FK_documentStatusId = 7 " +
            "WHERE rr.FK_documentStatusId = 7 " +
            "AND (rr.id NOT IN (SELECT COALESCE(AccountSetting.FK_receivingReportId, 0) FROM accountsetting) " +
            "OR cal.id IS NOT NULL " +
            "OR cv.id IS NOT NULL " +
            "OR po.useCreditCard) " +
            "GROUP BY rr.id  " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(DISTINCT rr.id) " +
                    "FROM ReceivingReport rr " +
                    "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
                    "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
                    "JOIN PurchaseOrder po ON pod.FK_purchaseOrderId = po.id " +
                    "LEFT JOIN CashAdvance ca ON po.FK_cashAdvanceId = ca.id " +
                    "LEFT JOIN CashAdvanceLiquidation cal ON ca.id = cal.FK_cashAdvanceId AND cal.FK_documentStatusId = 7 " +
                    "LEFT JOIN CheckVoucher cv ON po.id = cv.FK_purchaseOrderId AND cal.FK_documentStatusId = 7 " +
                    "WHERE rr.FK_documentStatusId = 7 " +
                    "AND (rr.id NOT IN (SELECT COALESCE(AccountSetting.FK_receivingReportId, 0) FROM accountsetting) " +
                    "OR cal.id IS NOT NULL " +
                    "OR cv.id IS NOT NULL " +
                    "OR po.useCreditCard)",
            nativeQuery = true)
    Page<ReceivingReport> findAllForApv(Pageable pageable);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM ReceivingReport   " +
            "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id " +
            "JOIN Supplier ON ReceivingReport.FK_supplierId = Supplier.id  " +
            "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
            "AND (ReceivingReport.confirmedForJv = 0 OR ReceivingReport.confirmedForJv IS NULL) " +
            "AND Supplier.FK_accountNo = :supplierId " +
            "AND UPPER(ReceivingReport.code) LIKE :query OR UPPER(Supplier.name) LIKE :query  " +
            "AND ReceivingReport.id IN (SELECT AccountSetting.FK_receivingReportId FROM AccountSetting) " +
            "AND ReceivingReport.id NOT IN (  " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a  " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id  " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ")  " +
            "AND PurchaseOrder.id NOT IN ( " +
            "  SELECT  " +
            "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
            "  FROM CreditCardPurchaseRequest  " +
            "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
            ") " +
            "GROUP BY ReceivingReport.id " +
            "ORDER BY ReceivingReport.`code` " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM ReceivingReport   " +
                    "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
                    "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
                    "JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id " +
                    "JOIN Supplier ON ReceivingReport.FK_supplierId = Supplier.id  " +
                    "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
                    "AND (ReceivingReport.confirmedForJv = 0 OR ReceivingReport.confirmedForJv IS NULL) " +
                    "AND Supplier.FK_accountNo = :supplierId " +
                    "AND UPPER(ReceivingReport.code) LIKE :query OR UPPER(Supplier.name) LIKE :query " +
                    "AND ReceivingReport.id IN (SELECT AccountSetting.FK_receivingReportId FROM AccountSetting)" +
                    "AND ReceivingReport.id NOT IN (  " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a  " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id  " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ")  " +
                    "AND PurchaseOrder.id NOT IN ( " +
                    "  SELECT  " +
                    "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
                    "  FROM CreditCardPurchaseRequest  " +
                    "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
                    ") " +
                    "GROUP BY ReceivingReport.id",
            nativeQuery = true)
    Page<ReceivingReport> findAllByQueryForApvWithAccountSetting(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, @Param("supplierId") Integer supplierId, Pageable pageable);

    @Query(value = "SELECT   " +
            "ReceivingReport.*  " +
            "FROM ReceivingReport " +
            "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
            "JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id " +
            "JOIN Supplier ON ReceivingReport.FK_supplierId = Supplier.id  " +
            "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
            "AND (ReceivingReport.confirmedForJv = 0 OR ReceivingReport.confirmedForJv IS NULL)  " +
            "AND Supplier.id = :supplierId  " +
            "AND ReceivingReport.id IN (SELECT AccountSetting.FK_receivingReportId FROM AccountSetting)  " +
            "AND ReceivingReport.id NOT IN (  " +
            "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a  " +
            "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id  " +
            "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
            ")  " +
            "AND PurchaseOrder.id NOT IN ( " +
            "  SELECT  " +
            "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
            "  FROM CreditCardPurchaseRequest  " +
            "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
            ") " +
            "GROUP BY ReceivingReport.id " +
            "ORDER BY ReceivingReport.`code` " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM ReceivingReport " +
                    "JOIN ReceivingReportDetail ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
                    "JOIN PoDetail ON ReceivingReportDetail.FK_poDetailId = PoDetail.id " +
                    "JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id " +
                    "JOIN Supplier ON ReceivingReport.FK_supplierId = Supplier.id  " +
                    "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
                    "AND (ReceivingReport.confirmedForJv = 0 OR ReceivingReport.confirmedForJv IS NULL)  " +
                    "AND Supplier.id = :supplierId  " +
                    "AND ReceivingReport.id IN (SELECT AccountSetting.FK_receivingReportId FROM AccountSetting)  " +
                    "AND ReceivingReport.id NOT IN (  " +
                    "  SELECT a.FK_linkedDocumentId FROM AccountsPayableVoucherLink a  " +
                    "  INNER JOIN AccountsPayableVoucher apv ON a.FK_accountsPayableVoucherId = apv.id  " +
                    "  WHERE apv.FK_documentStatusId != 26 AND a.FK_documentTypeId = :docTypeId " +
                    ")  " +
                    "AND PurchaseOrder.id NOT IN ( " +
                    "  SELECT  " +
                    "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
                    "  FROM CreditCardPurchaseRequest  " +
                    "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
                    ") " +
                    "GROUP BY ReceivingReport.id",
            nativeQuery = true)
    Page<ReceivingReport> findAllForApvWithAccountSetting(@Param("documentStatusId") Integer documentStatusId, @Param("docTypeId") Integer docTypeId, @Param("supplierId") Integer supplierId, Pageable pageable);

    @Query(value = "SELECT rr.* FROM ReceivingReport rr " +
            "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
            "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
            "JOIN PurchaseOrder po ON po.id = pod.FK_purchaseOrderId " +
            "WHERE rr.FK_documentStatusId = :documentStatusId " +
            "AND (po.FK_vehicleId > 0 OR po.FK_vehicleId IS NOT NULL) " +
            "AND rr.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherRr.FK_receivingReportId  " +
            "  FROM CheckVoucherRr " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherRr.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "AND po.id NOT IN ( " +
            "  SELECT  " +
            "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
            "  FROM CreditCardPurchaseRequest  " +
            "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
            ") " +
            "GROUP BY rr.id " +
            "ORDER BY rr.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ReceivingReport rr " +
                    "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
                    "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
                    "JOIN PurchaseOrder po ON po.id = pod.FK_purchaseOrderId " +
                    "WHERE rr.FK_documentStatusId = :documentStatusId " +
                    "AND (po.FK_vehicleId > 0 OR po.FK_vehicleId IS NOT NULL) " +
                    "AND rr.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherRr.FK_receivingReportId  " +
                    "  FROM CheckVoucherRr " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherRr.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") " +
                    "AND po.id NOT IN ( " +
                    "  SELECT  " +
                    "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
                    "  FROM CreditCardPurchaseRequest  " +
                    "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
                    ") " +
                    "GROUP BY rr.id " +
                    "ORDER BY rr.code ",
            nativeQuery = true)
    Page<ReceivingReport> findAllByDocumentStatusForCv(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT rr.* FROM ReceivingReport rr " +
            "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
            "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
            "JOIN PurchaseOrder po ON po.id = pod.FK_purchaseOrderId " +
            "WHERE rr.FK_documentStatusId = :documentStatusId " +
            "AND (po.FK_vehicleId > 0 OR po.FK_vehicleId IS NOT NULL) " +
            "AND rr.id NOT IN ( " +
            "  SELECT  " +
            "  CheckVoucherRr.FK_receivingReportId  " +
            "  FROM CheckVoucherRr " +
            "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherRr.FK_checkVoucherId " +
            "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
            ") " +
            "AND po.id NOT IN ( " +
            "  SELECT  " +
            "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
            "  FROM CreditCardPurchaseRequest  " +
            "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
            ") " +
            "AND rr.code LIKE :query " +
            "GROUP BY rr.id " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) FROM ReceivingReport rr " +
                    "JOIN ReceivingReportDetail rrd ON rr.id = rrd.FK_receivingReportId " +
                    "JOIN PoDetail pod ON pod.id = rrd.FK_poDetailId " +
                    "JOIN PurchaseOrder po ON po.id = pod.FK_purchaseOrderId " +
                    "WHERE rr.FK_documentStatusId = :documentStatusId " +
                    "AND (po.FK_vehicleId > 0 OR po.FK_vehicleId IS NOT NULL) " +
                    "AND rr.id NOT IN ( " +
                    "  SELECT  " +
                    "  CheckVoucherRr.FK_receivingReportId  " +
                    "  FROM CheckVoucherRr " +
                    "  INNER JOIN CheckVoucher ON CheckVoucher.id = CheckVoucherRr.FK_checkVoucherId " +
                    "  WHERE CheckVoucher.FK_documentStatusId NOT IN (8, 26) " +
                    ") " +
                    "AND po.id NOT IN ( " +
                    "  SELECT  " +
                    "  CreditCardPurchaseRequest.FK_purchaseOrderId  " +
                    "  FROM CreditCardPurchaseRequest  " +
                    "  WHERE CreditCardPurchaseRequest.FK_documentStatusId != 26 " +
                    ") " +
                    "AND rr.code LIKE :query " +
                    "GROUP BY rr.id ",
            nativeQuery = true)
    Page<ReceivingReport> findAllByQueryAndDocumentStatusForCv(@Param("query") String query,
                                                               @Param("documentStatusId") Integer documentStatusId,
                                                               Pageable pageable);

    @Query(value = "SELECT   " +
            "*  " +
            "FROM ReceivingReport   " +
            "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
            "AND UPPER(ReceivingReport.code) LIKE :query  " +
            "AND ReceivingReport.confirmedForJv " +
            "AND ReceivingReport.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) " +
            "\n#pageable\n",
            countQuery = "SELECT   " +
                    "COUNT(*)  " +
                    "FROM ReceivingReport   " +
                    "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
                    "AND UPPER(ReceivingReport.code) LIKE :query  " +
                    "AND ReceivingReport.confirmedForJv " +
                    "AND ReceivingReport.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher)  ",
            nativeQuery = true)
    Page<ReceivingReport> findAllByQueryForJV(@Param("query") String query, @Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Query(value = "SELECT  " +
            "* " +
            "FROM ReceivingReport   " +
            "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
            "AND ReceivingReport.confirmedForJv " +
            "AND ReceivingReport.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher)  " +
            "\n#pageable\n",
            countQuery = "SELECT  " +
                    "COUNT(*) " +
                    "FROM ReceivingReport   " +
                    "WHERE ReceivingReport.FK_documentStatusId = :documentStatusId  " +
                    "AND ReceivingReport.confirmedForJv " +
                    "AND ReceivingReport.FK_transactionId NOT IN (SELECT COALESCE(JournalVoucher.invDocTransactionId, 0) FROM JournalVoucher) ",
            nativeQuery = true)
    Page<ReceivingReport> findAllForJV(@Param("documentStatusId") Integer documentStatusId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE ReceivingReport r SET r.confirmedForJv = :confirmed WHERE r.id = :id")
    void updateConfirmedForJv(@Param("id") Integer id, @Param("confirmed") boolean confirmed);
}
