package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PurchaseRequest;
import com.noreco1.fireflyv2.model.PurchaseRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface PurchaseRequestDetailRepo extends JpaRepository<PurchaseRequestDetail, Integer> {

    public List<PurchaseRequestDetail> findByPurchaseRequestId(Integer rvId);
    PurchaseRequestDetail findByPurchaseRequestIdAndItemId(Integer rvId, Integer itemId);

    public Long deleteByPurchaseRequestId(Integer transId);

    @Transactional
    @Query(value = "SELECT rvd.* FROM PurchaseRequestDetail rvd " +
            "INNER JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "WHERE e.FK_documentStatusId = :statusId AND (rvd.quantity - rvd.poQuantity) != 0", nativeQuery = true)
    public List<PurchaseRequestDetail> findPurchaseRequestDetailsByPurchaseRequestDocumentStatusId(@Param("statusId")Integer statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE PurchaseRequestDetail SET poQuantity = :poQty WHERE id = :rvdId", nativeQuery = true)
    public int updatePoQuantityById(@Param("rvdId") Integer id, @Param("poQty") BigDecimal poQuantity);

    @Query(value = "SELECT * FROM " +
            "(SELECT rvd.* FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "JOIN QuotationItem ON rvd.id = QuotationItem.FK_PurchaseRequestDetailId " +
            "JOIN Quotation q ON q.id = QuotationItem.FK_quotationId " +
            "WHERE (e.rvType = :rvType1 || e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = 55 " + // status: Reviewed and Accepted
            "AND q.FK_documentStatusId = 7 " + // status: Approved
            "AND QuotationItem.isAvailable group by rvd.id " +
            "UNION " +
            "SELECT rvd.* FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "WHERE (e.rvType = :rvType1 OR e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = 55 " + // status: Reviewed and Accepted
            "AND e.FK_modeOfProcurementId IN (3, 4) " + // mode: Public Bidding and Accredited Supplier
            "group by rvd.id) as items " +
            "ORDER BY id", nativeQuery = true)
    public List<PurchaseRequestDetail> findPurchaseRequestDetailsByTypesAndStatusId(@Param("rvType1")Integer rvType1, @Param("rvType2")Integer rvType2);

    @Query(value = "SELECT * FROM (" +
            "SELECT rvd.*, voucherDate FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "JOIN QuotationDetail ON rvd.id = QuotationDetail.FK_PurchaseRequestDetailId " +
            "JOIN Quotation q ON q.id = QuotationDetail.FK_quotationId " +
            "WHERE (e.rvType = :rvType1 || e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = :statusId " +
            "AND q.FK_documentStatusId = :statusId AND QuotationDetail.isAvailable group by rvd.id " +
            "UNION " +
            "SELECT rvd.*, voucherDate FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "WHERE (e.rvType = :rvType1 OR e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = :statusId " +
            "AND e.FK_modeOfProcurementId IN (5,6) group by rvd.id) as vouchers " +
            "ORDER BY voucherDate DESC", nativeQuery = true) // mode: BIDDING
    List<PurchaseRequestDetail> findPurchaseRequestDetailsByTypesAndStatusIdOrderByVoucherDateDesc(@Param("rvType1")Integer rvType1, @Param("rvType2")Integer rvType2, @Param("statusId")Integer statusId);

    @Transactional
    @Query(value = "SELECT rvd.*, rrd.unitPrice, rrd.vat FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "JOIN ReceivingReportDetail rrd on rvd.FK_itemId = rrd.FK_itemId " +
            "JOIN ReceivingReport rr on rrd.FK_receivingReportId = rr.id " +
            "WHERE (e.rvType = :rvType1 || e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 AND e.FK_documentStatusId = :statusId " +
            "AND rvd.id not in (select FK_PurchaseRequestDetailId from QuotationDetail where QuotationDetail.isAvailable) " +
            "AND rr.FK_documentStatusId = :statusId " +
            "group by rvd.id", nativeQuery = true)
    public List<Object[]> findNoQuotationPurchaseRequestDetailsByTypesAndStatusId(@Param("rvType1")Integer rvType1, @Param("rvType2")Integer rvType2, @Param("statusId")Integer statusId);

    @Transactional
    @Query(value = "SELECT rvd.*, rrd.unitPrice, rrd.vat FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "JOIN ReceivingReportDetail rrd on rvd.FK_itemId = rrd.FK_itemId " +
            "JOIN ReceivingReport rr on rrd.FK_receivingReportId = rr.id " +
            "WHERE (e.rvType = :rvType1 || e.rvType = :rvType2) AND (rvd.quantity - rvd.poQuantity) > 0 AND e.FK_documentStatusId = :statusId " +
            "AND rvd.id not in (select FK_PurchaseRequestDetailId from QuotationDetail where QuotationDetail.isAvailable) " +
            "AND rr.FK_documentStatusId = :statusId " +
            "AND rr.FK_supplierId = :supplierId " +
            "group by rvd.id", nativeQuery = true)
    public List<Object[]> findNoQuotationPurchaseRequestDetailsByTypesAndStatusIdAndSupplierId(@Param("rvType1")Integer rvType1, @Param("rvType2")Integer rvType2, @Param("statusId")Integer statusId, @Param("supplierId")Integer supplierId);


    @Query("SELECT rvd.id, rvd.quantity, rvd.poQuantity, rvd.joDescription, " +
            "rvd.item.id, rvd.item.code, rvd.item.description, " +
            "rvd.unitMeasure.id, rvd.unitMeasure.code, " +
            "rvd.purchaseRequest.id, rvd.purchaseRequest.code, rvd.purchaseRequest.voucherDate, " +
            "rvd.purchaseRequest.createdBy.fullName " +
            "FROM PurchaseRequestDetail rvd " +
            "WHERE rvd.purchaseRequest.rvType IN (:rvTypes) " +
            "AND (rvd.quantity - rvd.poQuantity) <> 0 " +
            "AND rvd.purchaseRequest.documentStatus.id = :approvedStatusId " +
            "AND (rvd.purchaseRequest.emergencyPurchase = false OR rvd.purchaseRequest.emergencyPurchase IS NULL) " +
            "AND rvd.purchaseRequest.id = :prId " +
            "AND NOT EXISTS (" +
                "SELECT cd FROM CanvassDetail cd " +
                "WHERE cd.purchaseRequestDetail = rvd " +
                "AND cd.canvass.documentStatus.id <> :cancelledStatusId" +
            ")")
    List<Object[]> findPrdForCanvass(@Param("rvTypes") List rvTypes,
                                     @Param("approvedStatusId") Integer approvedStatusId,
                                     @Param("cancelledStatusId") Integer cancelledStatusId,
                                     @Param("prId") Integer prId);

    @Query("SELECT rvd.id, rvd.quantity, rvd.poQuantity, rvd.joDescription, " +
            "rvd.item.id, rvd.item.code, rvd.item.description, " +
            "rvd.unitMeasure.id, rvd.unitMeasure.code, " +
            "rvd.purchaseRequest.id, rvd.purchaseRequest.code, rvd.purchaseRequest.voucherDate, " +
            "rvd.purchaseRequest.createdBy.fullName " +
            "FROM PurchaseRequestDetail rvd " +
            "WHERE rvd.purchaseRequest.rvType IN (:rvTypes) " +
            "AND (rvd.quantity - rvd.poQuantity) <> 0 " +
            "AND rvd.purchaseRequest.documentStatus.id = :approvedStatusId " +
            "AND (rvd.purchaseRequest.emergencyPurchase = false OR rvd.purchaseRequest.emergencyPurchase IS NULL) " +
            "AND NOT EXISTS (" +
                "SELECT cd FROM CanvassDetail cd " +
                "WHERE cd.purchaseRequestDetail = rvd " +
                "AND cd.canvass.documentStatus.id <> :cancelledStatusId" +
            ") " +
            "ORDER BY rvd.purchaseRequest.code")
    List<Object[]> findAllPrdForCanvass(@Param("rvTypes") List rvTypes,
                                        @Param("approvedStatusId") Integer approvedStatusId,
                                        @Param("cancelledStatusId") Integer cancelledStatusId);

    @Query("SELECT rvd.id, rvd.quantity, rvd.poQuantity, rvd.joDescription, " +
            "rvd.item.id, rvd.item.code, rvd.item.description, " +
            "rvd.unitMeasure.id, rvd.unitMeasure.code, " +
            "rvd.purchaseRequest.id, rvd.purchaseRequest.code, rvd.purchaseRequest.voucherDate, " +
            "rvd.purchaseRequest.createdBy.fullName " +
            "FROM PurchaseRequestDetail rvd " +
            "WHERE rvd.purchaseRequest.rvType IN (:rvTypes) " +
            "AND rvd.purchaseRequest.documentStatus.id = :approvedStatusId " +
            "AND EXISTS (" +
                "SELECT cd FROM CanvassDetail cd " +
                "WHERE cd.purchaseRequestDetail = rvd " +
                "AND cd.canvass.documentStatus.id <> :cancelledStatusId" +
            ") " +
            "ORDER BY rvd.purchaseRequest.code")
    List<Object[]> findAllPrdForQuotation(@Param("rvTypes") List rvTypes,
                                          @Param("approvedStatusId") Integer approvedStatusId,
                                          @Param("cancelledStatusId") Integer cancelledStatusId);

    @Query("SELECT rvd.id, rvd.quantity, rvd.poQuantity, rvd.joDescription, " +
            "rvd.item.id, rvd.item.code, rvd.item.description, " +
            "rvd.unitMeasure.id, rvd.unitMeasure.code, " +
            "rvd.purchaseRequest.id, rvd.purchaseRequest.code, rvd.purchaseRequest.voucherDate, rvd.purchaseRequest.purpose " +
            "FROM PurchaseRequestDetail rvd " +
            "WHERE rvd.purchaseRequest.rvType IN (:rvTypes) " +
            "AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND rvd.purchaseRequest.documentStatus.id = 55")
    List<Object[]> findPurchaseRequestDetailsForJo(@Param("rvTypes") List<Integer> rvTypes);

    @Query(value = "SELECT DISTINCT e.id, e.code, e.voucherDate, e.purpose, e.deliveryDate, u.fullName " +
            "FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "LEFT JOIN `User` u ON e.FK_createdByUserId = u.id " +
            "WHERE e.rvType IN (:rvTypes) " +
            "AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = 55 " +
            "ORDER BY e.code", nativeQuery = true)
    List<Object[]> findPurchaseRequestsForJo(@Param("rvTypes") List<Integer> rvTypes);

    @Query(value = "SELECT DISTINCT e.id, e.code, e.voucherDate, e.purpose, e.deliveryDate, u.fullName " +
            "FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "LEFT JOIN `User` u ON e.FK_createdByUserId = u.id " +
            "WHERE e.rvType IN (:rvTypes) " +
            "AND (rvd.quantity - rvd.poQuantity) > 0 " +
            "AND e.FK_documentStatusId = 55 " +
            "ORDER BY e.code", nativeQuery = true)
    List<Object[]> findPurchaseRequestsForPo(@Param("rvTypes") List<Integer> rvTypes);

    @Query(value = "SELECT " +
            "*, " +
            "if(rvd.FK_itemId > 0, 1, 2) AS itemGroup " +
            "FROM PurchaseRequestDetail rvd " +
            "WHERE rvd.FK_PurchaseRequestId = :rvId ORDER BY itemGroup, rvd.id", nativeQuery = true)
    public List<PurchaseRequestDetail> findByPurchaseRequestIdOrderByItemGroupAndId(@Param("rvId") Integer rvId);

    @Query(value = "SELECT " +
            "ist.quantity AS stockQty, " +
            "ist.id AS itemStockId, " +
            "i.id AS itemId, " +
            "i.code, " +
            "u.code AS unitCode, " +
            "u.id AS unitId, " +
            "ist.unitCost, " +
            "i.description, " +
            "ist.FK_inventoryLocationId, " +
            "rd.quantity, " +
            "SUM(COALESCE(pd.deliveredQuantity, 0)) - rd.withdrawQuantity AS withdrawableQty, " +
            "SUM(COALESCE(pd.deliveredQuantity, 0)), " +
            "rd.withdrawQuantity, " +
            "pd.id as poDetailId " +
            "FROM PurchaseRequestDetail rd " +
            "LEFT JOIN PoDetail pd ON rd.id = pd.FK_purchaseRequestDetailId " +
            "INNER JOIN ItemStock ist ON ist.FK_itemId = rd.FK_itemId " +
            "INNER JOIN Item i ON ist.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "WHERE rd.FK_purchaseRequestId = :rvId " +
            "AND ist.FK_inventoryLocationId = :invLocId " +
            "AND i.FK_inventoryCategoryId = :invCatId " +
            "GROUP BY rd.id " +
            "HAVING SUM(pd.deliveredQuantity) > rd.withdrawQuantity OR (poDetailId IS NULL AND rd.withdrawquantity<rd.quantity)", nativeQuery = true)
    List<Object[]> findForWithdrawal(@Param("rvId") Integer rvId,
                                     @Param("invLocId") Integer invLocId,
                                     @Param("invCatId") Integer invCatId);

    @Query(value = "SELECT " +
            "            ist.totalQuantity AS stockQty, " +
            "            ist.id AS itemStockId, " +
            "            i.id AS itemId, " +
            "            i.code, " +
            "            u.code AS unitCode, " +
            "            u.id AS unitId, " +
            "            ist.unitCost, " +
            "            i.description, " +
            "            ist.FK_inventoryLocationId, " +
            "            rd.quantity, " +
            "SUM(pd.deliveredQuantity) - rd.withdrawQuantity AS withdrawableQty, SUM(pd.deliveredQuantity), rd.withdrawQuantity " +
            "FROM PurchaseRequestDetail rd " +
            "INNER JOIN PoDetail pd ON rd.id = pd.FK_PurchaseRequestDetailId " +
            "INNER JOIN ItemStock ist ON ist.FK_itemId = rd.FK_itemId " +
            "INNER JOIN Item i ON ist.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "WHERE rd.FK_PurchaseRequestId = :rvId " +
            "AND rd.FK_itemId = :itemId " +
            "GROUP BY rd.id", nativeQuery = true)
    List<Object[]> findForWithdrawalUpdate(@Param("rvId") Integer rvId,
                                           @Param("itemId") Integer itemId);

    @Query(value = "SELECT " +
            "            rd.id, " +
            "            rd.quantity, " +
            "            rd.rrQuantity, " +
            "            i.id AS itemId, " +
            "            i.code, " +
            "            i.description, " +
            "            u.code AS unitCode " +
            "FROM PurchaseRequestDetail rd " +
            "INNER JOIN Item i ON rd.FK_itemId = i.id " +
            "INNER JOIN UnitMeasure u ON i.FK_unitId = u.id " +
            "WHERE rd.FK_PurchaseRequestId = :rvId", nativeQuery = true)
    List<Object[]> findForRR(@Param("rvId") Integer rvId);

    @Query(value = "SELECT rvd.* FROM PurchaseRequestDetail rvd " +
            "JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id " +
            "JOIN PoDetail ON rvd.id = PoDetail.FK_PurchaseRequestDetailId " +
            "WHERE PoDetail.FK_purchaseOrderId = :poId " +
            "group by rvd.id", nativeQuery = true)
    public List<PurchaseRequestDetail> findPurchaseRequestDetailsByCancelledPOId(@Param("poId")Integer poId);

    @Query(value = "SELECT * FROM ( " +
            " " +
            "  SELECT rvd.* FROM PurchaseRequestDetail rvd  " +
            "  JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id  " +
            "  JOIN QuotationItem qi ON qi.FK_purchaseRequestDetailId = rvd.id " +
            "  JOIN Quotation q ON q.id = qi.FK_quotationId " +
            "  WHERE (e.rvType = :rvType1 || e.rvType = :rvType2)  " +
            "  AND (rvd.quantity - rvd.poQuantity) > 0  " +
            "  AND e.FK_documentStatusId = 55 " + // PR Reviewed and Accepted
            "  AND q.FK_documentStatusId = 7 " + // Quotation Approved
            "  GROUP BY rvd.id " +
            "   " +
            "  UNION  " +
            "   " +
            "  SELECT rvd.* FROM PurchaseRequestDetail rvd  " +
            "  JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id  " +
            "  WHERE (rvd.quantity - rvd.poQuantity) > 0 " +
            "  AND e.FK_documentStatusId = 55 " + // PR Reviewed and Accepted
            "  AND e.emergencyPurchase IS TRUE OR e.FK_modeOfProcurementId IN (2, 3) " + //PR for emergency purchase OR Mode of Procurement (2=SEALED_CANVASS, 3= PUBLIC_BIDDING)
            "  GROUP BY rvd.id " +
            " " +
            ") AS prForPO ", nativeQuery = true)
    List<PurchaseRequestDetail> findRvDetailsByTypesAndStatusId(@Param("rvType1")Integer rvType1, @Param("rvType2")Integer rvType2);

    @Query(value = "SELECT * FROM ( " +
            " " +
            "  SELECT rvd.* FROM PurchaseRequestDetail rvd  " +
            "  JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id  " +
            "  JOIN QuotationItem qi ON qi.FK_purchaseRequestDetailId = rvd.id " +
            "  JOIN QuotationItemDetail qid ON qid.FK_quotationItemId = qi.id " +
            "  JOIN Quotation q ON q.id = qi.FK_quotationId " +
            "  JOIN Supplier s ON s.id = qid.FK_supplierId " +
            "  WHERE (e.rvType = 1 || e.rvType = 2)  " + // RvType FOR_PO(1, "For PO"), FOR_IT(2, "For IT")
            "  AND (rvd.quantity - rvd.poQuantity) > 0  " +
            "  AND e.FK_documentStatusId = 55 " + // PR Reviewed and Accepted
            "  AND q.FK_documentStatusId = 7 " + // Quotation Approved
            "  AND e.id = :prId " +
            "  AND (s.FK_accountNo = :supplierAccountNumber AND qid.isAwarded) " +
            "  GROUP BY rvd.id " +
            "   " +
            "  UNION  " +
            "   " +
            "  SELECT rvd.* FROM PurchaseRequestDetail rvd  " +
            "  JOIN PurchaseRequest e ON rvd.FK_PurchaseRequestId = e.id  " +
            "  WHERE (rvd.quantity - rvd.poQuantity) > 0 " +
            "  AND e.FK_documentStatusId = 55 " + // PR Reviewed and Accepted
            "  AND e.emergencyPurchase IS TRUE OR e.FK_modeOfProcurementId IN (2, 3) " + //PR for emergency purchase OR Mode of Procurement (2=SEALED_CANVASS, 3= PUBLIC_BIDDING)
            "  AND e.id = :prId " +
            "  GROUP BY rvd.id " +
            " " +
            ") AS prForPO ", nativeQuery = true)
    List<PurchaseRequestDetail> findPrDetailsByTypesAndStatusId(@Param("prId")Integer prId, @Param("supplierAccountNumber")Integer supplierAccountNumber);


    @Transactional
    @Query(value =
            "SELECT pr.* " +
            "FROM PurchaseRequest pr " +
            "JOIN PurchaseRequestDetail prd ON prd.FK_purchaseRequestId = pr.id " +
            "WHERE pr.rvType IN (:rvTypes) " +
            "AND (prd.quantity - prd.poQuantity) != 0 " +
            "AND pr.FK_documentStatusId = :approvedStatusId " +
            "AND (pr.emergencyPurchase IS FALSE OR pr.emergencyPurchase IS NULL) " +
            "AND prd.id NOT IN ( " +
            "  SELECT FK_PurchaseRequestDetailId FROM CanvassDetail cd " +
            "  INNER JOIN Canvass c ON cd.FK_canvassId = c.id " +
            "  WHERE c.FK_documentStatusId != :cancelledStatusId " +
            ") " +
            "GROUP BY pr.id, prd.id " +
            "ORDER BY pr.code ", nativeQuery = true)
    List<PurchaseRequest> findAllPurchaseRequestForCanvass(@Param("rvTypes") List rvTypes,
                                                           @Param("approvedStatusId")Integer approvedStatusId,
                                                           @Param("cancelledStatusId")Integer cancelledStatusId);


}
