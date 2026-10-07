package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 5/15/2015.
 */
public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Integer> {
    public List<PurchaseOrder> findByCode(String code);
    @Query(value = "SELECT e.code FROM PurchaseOrder e WHERE year = :year  AND code LIKE '%PO%' ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestPoCodeByYear(@Param("year") Integer year);

    @Query(value = "SELECT " +
            "po.id, " +
            "po.code, " +
            "po.voucherDate, " +
            "sup.name, " +
            "po.term, " +
            "po.amount, " +
            "(SELECT COUNT(id) FROM PurchaseOrderDetail WHERE FK_purchaseOrderId = po.id) AS noOfItems, " +
            "d.status, " +
            "u.fullName " +
            "FROM PurchaseOrder po " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = po.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = po.FK_documentStatusId " +
            "LEFT JOIN User u ON u.id = po.FK_createdByUserId " +
            "WHERE po.voucherDate >= :from AND po.voucherDate <= :to " +
            "AND po.FK_documentStatusId = :documentStatusId " +
            "ORDER BY po.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "po.id, " +
            "po.code, " +
            "po.voucherDate, " +
            "sup.name, " +
            "po.term, " +
            "po.amount, " +
            "(SELECT COUNT(id) FROM PurchaseOrderDetail WHERE FK_purchaseOrderId = po.id) AS noOfItems, " +
            "d.status, " +
            "u.fullName " +
            "FROM PurchaseOrder po " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = po.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = po.FK_documentStatusId " +
            "LEFT JOIN User u ON u.id = po.FK_createdByUserId " +
            "WHERE po.voucherDate >= :from AND po.voucherDate <= :to " +
            "ORDER BY po.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    public PurchaseOrder findOneByTransactionId(Integer id);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.voucherDate, " +
            "sup.name, " +
            "doc.term, " +
            "doc.amount, " +
            "(SELECT COUNT(id) FROM PurchaseOrderDetail WHERE FK_purchaseOrderId = doc.id) AS noOfItems, " +
            "d.status, " +
            "u.fullName " +
            "FROM PurchaseOrder doc " +
            "INNER JOIN Supplier sup ON sup.FK_accountNo = doc.FK_vendorAccountNo " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "LEFT JOIN User u ON u.id = doc.FK_createdByUserId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId != :documentStatusId " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);
    List<PurchaseOrder> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);
    List<PurchaseOrder> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    List<PurchaseOrder> findByVoucherDateBetweenAndDocumentStatusIdNotInAndOfficeId(Date from, Date to, Collection<Integer> documentStatusIds, Integer officeId);
    List<PurchaseOrder> findByDocumentStatusIdAndVoucherDateBetweenAndOfficeId(Integer statusId, Date from, Date to, Integer officeId);
    PurchaseOrder findFirstByOrderByIdAsc();

    @Query(value = "SELECT * " +
            "FROM PurchaseOrder p " +
            "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
            "WHERE p.FK_documentStatusId = :documentStatusId " +
            "AND p.id IN (SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity + " +
            "(SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
            "WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter) " +
            "AND p.id NOT IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it WHERE it.FK_purchaseOrderId IS NOT null) " +
            "ORDER BY p.code" +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseOrder p " +
                    "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
                    "WHERE p.FK_documentStatusId = :documentStatusId " +
                    "AND p.id IN (SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity + " +
                    "(SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
                    "WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter)" +
                    "AND p.id NOT IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it WHERE it.FK_purchaseOrderId IS NOT null) ",
            nativeQuery = true)
    Page<PurchaseOrder> findByStatusAndFilter(@Param("documentStatusId") Integer documentStatusId,
                                              @Param("filter") String filter, Pageable pageable);

    List<PurchaseOrder> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<PurchaseOrder> findByDocumentStatusId(Integer status);

    // use of checking if PO has items delivered or RR
    @Query(value = "SELECT PurchaseOrder.id as poid, PurchaseOrderDetail.id as podId, ReceivingReportDetail.id as rrdId from PurchaseOrder " +
            "LEFT JOIN PurchaseOrderDetail ON PurchaseOrder.id = PurchaseOrderDetail.FK_purchaseOrderId " +
            "LEFT JOIN ReceivingReportDetail ON PurchaseOrderDetail.id = ReceivingReportDetail.FK_PurchaseOrderDetailId " +
            "LEFT JOIN ReceivingReport ON ReceivingReport.id = ReceivingReportDetail.FK_receivingReportId " +
            "WHERE PurchaseOrder.id = :poId " +
            "AND ReceivingReport.FK_documentStatusId NOT IN (8, 26) " +
            "LIMIT 1", nativeQuery = true)
    List<Object[]> findRRDetailById(@Param("poId") Integer poId);

    List<PurchaseOrder> findByDocumentStatusIdOrderByIdDesc(Integer status);

    @Query(value = "SELECT p.* " +
            "FROM PurchaseOrder p " +
            "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
            "WHERE p.FK_documentStatusId = :documentStatusId " +
            "AND p.id IN ( " +
            "  SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity " +
            "  + " +
            "  ( " +
            "  SELECT " +
            "  COALESCE(SUM(itd.unitsReceivedQuantity), 0) " +
            "  FROM ItemTestingDetail itd " +
            "  WHERE itd.FK_PurchaseOrderDetailId = pod.id " +
            "  ) " +
            ") " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter) ",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseOrder p " +
                    "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
                    "WHERE p.FK_documentStatusId = :documentStatusId " +
                    "AND p.id IN ( " +
                    "  SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity " +
                    "  + " +
                    "  ( " +
                    "  SELECT " +
                    "  COALESCE(SUM(itd.unitsReceivedQuantity), 0) " +
                    "  FROM ItemTestingDetail itd " +
                    "  WHERE itd.FK_PurchaseOrderDetailId = pod.id " +
                    "  ) " +
                    ")  " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter) ", nativeQuery = true)
    Page<PurchaseOrder> findPurchaseOrderForItemTestingByFilter(@Param("documentStatusId") Integer documentStatusId,
                                                                @Param("filter") String filter, Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM PurchaseOrder p " +
            "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
            "WHERE p.FK_documentStatusId = :documentStatusId " +
            "AND p.id IN (SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity + " +
            "(SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
            "WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter) " +
            "AND p.id IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it) " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseOrder p " +
                    "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
                    "WHERE p.FK_documentStatusId = :documentStatusId " +
                    "AND p.id IN (SELECT pod.FK_purchaseOrderId FROM PurchaseOrderDetail pod WHERE pod.quantity > pod.deliveredQuantity + " +
                    "(SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
                    "WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))) " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter)" +
                    "AND p.id IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it) ",
            nativeQuery = true)
    Page<PurchaseOrder> findPurchaseOrderWithItemTestingForRRByFilter(@Param("documentStatusId") Integer documentStatusId,
                                                                      @Param("filter") String filter, Pageable pageable);

    @Query(value = "SELECT * FROM PurchaseOrder po " +
            "JOIN PurchaseOrderDetail pod ON po.id = pod.FK_purchaseOrderId " +
            "LEFT JOIN ReceivingReportDetail rrd ON pod.id = rrd.FK_PurchaseOrderDetailId " +
            "WHERE po.FK_documentStatusId = :statusId " +
            "AND rrd.id IS null", nativeQuery = true)
    public List<PurchaseOrder> findPurchaseOrdersForCV(@Param("statusId") Integer statusId);

    @Query(value = "SELECT * " +
            "FROM PurchaseOrder p " +
            "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
            "WHERE p.FK_documentStatusId = :documentStatusId " +
            "AND p.id IN (" +
            "  SELECT " +
            "  pod.FK_purchaseOrderId " +
            "  FROM PurchaseOrderDetail pod " +
            "  WHERE pod.quantity > pod.deliveredQuantity + " +
            "  (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
            "  WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))" +
            ") " +
            "AND (p.code LIKE :filter OR s.name LIKE :filter) " +
            "AND p.id NOT IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it WHERE it.FK_purchaseOrderId IS NOT NULL) " +
            "AND p.id NOT IN (SELECT ccpr.FK_purchaseOrderId FROM CreditCardPurchaseRequest ccpr WHERE ccpr.FK_purchaseOrderId IS NOT NULL) " +
            "ORDER BY p.code " +
            "\n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseOrder p " +
                    "INNER JOIN Supplier s ON s.FK_accountNo = p.FK_vendorAccountNo " +
                    "WHERE p.FK_documentStatusId = :documentStatusId " +
                    "AND p.id IN (" +
                    "  SELECT " +
                    "  pod.FK_purchaseOrderId " +
                    "  FROM PurchaseOrderDetail pod " +
                    "  WHERE pod.quantity > pod.deliveredQuantity + " +
                    "  (SELECT COALESCE(SUM(rrd.quantityReceived), 0) FROM ReceivingReportDetail rrd INNER JOIN ReceivingReport rr ON rrd.FK_receivingReportId = rr.id " +
                    "  WHERE rrd.FK_PurchaseOrderDetailId = pod.id AND rr.FK_documentStatusId NOT IN (7,8,26))" +
                    ") " +
                    "AND (p.code LIKE :filter OR s.name LIKE :filter) " +
                    "AND p.id NOT IN (SELECT it.FK_purchaseOrderId FROM ItemTesting it WHERE it.FK_purchaseOrderId IS NOT NULL) " +
                    "AND p.id NOT IN (SELECT ccpr.FK_purchaseOrderId FROM CreditCardPurchaseRequest ccpr WHERE ccpr.FK_purchaseOrderId IS NOT NULL) ",
            nativeQuery = true)
    Page<PurchaseOrder> findAllForCreditCardPurchaseRequestByStatusAndFilter(@Param("documentStatusId") Integer documentStatusId,
                                                                             @Param("filter") String filter, Pageable pageable);

    Page<PurchaseOrder> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    @Query(value = "SELECT DISTINCT po.* " +
            "FROM SlEntity s " +
            "INNER JOIN PurchaseOrder po ON po.FK_vendorAccountNo = s.accountNo " +
            "WHERE po.FK_documentStatusId = :statusId", nativeQuery = true)
    List<PurchaseOrder> findApprovedPurchaseOrderVendors(@Param("statusId") Integer statusId);

    @Query(value = "SELECT * FROM PurchaseOrder po " +
            "WHERE po.FK_vendorAccountNo = :accountNo " +
            "AND po.FK_documentStatusId = :statusId " +
            "ORDER BY po.id DESC", nativeQuery = true)
    List<PurchaseOrder> findApprovedPurchaseOrdersByVendor(@Param("accountNo") Integer accountNo,
                                                           @Param("statusId") Integer statusId);

}
