package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Dhokie on 10/6/2022.
 */
public interface PurchaseRequestRepo extends JpaRepository<PurchaseRequest, Integer> {

    public List<PurchaseRequest> findByCode(String code);

    @Query(value = "SELECT e.code FROM PurchaseRequest e WHERE year = :year AND code LIKE :prefix ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Object findLatestRvCodeByYear(@Param("year") Integer year, @Param("prefix") String prefix);

    @Transactional
    @Query(value = "SELECT " +
            "* " +
            "FROM PurchaseRequest rv " +
            "WHERE rv.id NOT IN " +
            "(SELECT rvs.id FROM PurchaseRequest rvs " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvs.id = rvd.FK_PurchaseRequestId " +
            "LEFT OUTER JOIN CanvassDetail cd ON rvd.id = cd.FK_purchaseRequestDetailId " +
            "WHERE rvs.FK_documentStatusId = :statusId AND cd.id IS NULL) " +
            "AND rv.FK_documentStatusId = :statusId", nativeQuery = true)
    public List<PurchaseRequest> findPurchaseRequestsForPO(@Param("statusId")Integer statusId);

    @Query(value = "SELECT " +
            "rv.id, " +
            "rv.code, " +
            "rv.purpose, " +
            "rv.voucherDate, " +
            "rv.deliveryDate, " +
            "(SELECT COUNT(id) FROM PurchaseRequestDetail WHERE FK_PurchaseRequestId = rv.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN User u ON u.id = rv.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = rv.FK_documentStatusId " +
            "WHERE rv.voucherDate >= :from AND rv.voucherDate <= :to " +
            "AND rv.FK_documentStatusId = :documentStatusId " +
            "ORDER BY rv.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatus(@Param("from") String from, @Param("to") String to, @Param("documentStatusId") Integer documentStatusId);

    @Query(value = "SELECT " +
            "rv.id, " +
            "rv.code, " +
            "rv.purpose, " +
            "rv.voucherDate, " +
            "rv.deliveryDate, " +
            "(SELECT COUNT(id) FROM PurchaseRequestDetail WHERE FK_PurchaseRequestId = rv.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN User u ON u.id = rv.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = rv.FK_documentStatusId " +
            "WHERE rv.voucherDate >= :from AND rv.voucherDate <= :to " +
            "ORDER BY rv.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRange(@Param("from") String from, @Param("to") String to);

    public PurchaseRequest findOneByTransactionId(Integer transId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.purpose, " +
            "doc.voucherDate, " +
            "doc.deliveryDate, " +
            "(SELECT COUNT(id) FROM PurchaseRequestDetail WHERE FK_PurchaseRequestId = doc.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM PurchaseRequest doc " +
            "INNER JOIN User u ON u.id = doc.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId NOT IN (:documentStatusId) " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusPending(@Param("from") String from, @Param("to") String to,
                                                                    @Param("documentStatusId") Integer... documentStatusId);

    @Query(value = "SELECT " +
            "doc.id, " +
            "doc.code, " +
            "doc.purpose, " +
            "doc.voucherDate, " +
            "doc.deliveryDate, " +
            "(SELECT COUNT(id) FROM PurchaseRequestDetail WHERE FK_PurchaseRequestId = doc.id) AS noOfItems, " +
            "u.fullName, " +
            "d.status " +
            "FROM PurchaseRequest doc " +
            "INNER JOIN User u ON u.id = doc.FK_createdByUserId " +
            "INNER JOIN DocumentStatus d ON d.id = doc.FK_documentStatusId " +
            "WHERE doc.voucherDate >= :from AND doc.voucherDate <= :to " +
            "AND doc.FK_documentStatusId IN (:documentStatusId) " +
            "ORDER BY doc.code", nativeQuery = true)
    public List<Object[]> findForSummaryByDateRangeAndStatusApproved(@Param("from") String from, @Param("to") String to,
                                                                     @Param("documentStatusId") Integer... documentStatusId);

    List<PurchaseRequest> findByVoucherDateBetweenAndDocumentStatusIdNotIn(Date from, Date to, Collection<Integer> documentStatusIds);

    @Query(value = "select " +
            "la.* " +
            "from PurchaseRequest la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` recommendUser on la.FK_recAppByUserId = recommendUser.id " +
            "LEFT JOIN `User` budgetOfficer on la.FK_budgetOfficerUserId = budgetOfficer.id " +
            "LEFT JOIN `User` canvassUser on la.FK_canvassedByUserId = canvassUser.id  " +
            "LEFT JOIN `User` conformUser on la.FK_conformedByUserId = conformUser.id " +
            "WHERE approveUser.id = :userId OR createUser.id = :userId OR recommendUser.id = :userId " +
            "OR budgetOfficer.id = :userId OR  canvassUser.id = :userId OR  conformUser.id = :userId",
            nativeQuery = true)
    List<PurchaseRequest> findAllByAllowedUsers(@Param("userId") Integer userId);

    @Transactional
    @Query(value = "SELECT " +
            "rv.id, " +
            "rv.voucherDate, " +
            "rv.deliveryDate, " +
            "rv.code as rvCode, " +
            "rv.purpose, " +
            "stat.status, " +
            "rv.rvType, " +
            "rv.FK_transactionId, " +
            "u.fullName, " +
            "ANY_VALUE(cnvs.code) as cCode " +
            "FROM PurchaseRequest rv " +
            "JOIN PurchaseRequestDetail rd ON rv.id = rd.FK_PurchaseRequestId " +
            "LEFT JOIN CanvassDetail cd ON rd.id = cd.FK_purchaseRequestDetailId " +
            "LEFT JOIN Canvass cnvs ON cnvs.id = cd.FK_canvassId " +
            "JOIN Documentstatus stat ON stat.id = rv.FK_documentStatusId " +
            "JOIN User u ON u.id = rv.FK_createdByUserId " +
            "WHERE rd.id NOT IN (SELECT FK_purchaseRequestDetailId FROM QuotationDetail) " +
            "AND cd.FK_canvassId IN (:canvassIds) GROUP BY rv.id ORDER BY rv.code, ANY_VALUE(cnvs.code)", nativeQuery = true)
    List<Object[]> findPurchaseRequestsForCanvass(@Param("canvassIds") List<Integer> canvassIds);

    public List<PurchaseRequest> findByDocumentStatusIdAndVoucherDateBetween(Integer statusId, Date from, Date to);
    PurchaseRequest findFirstByOrderByIdAsc();

    @Query(value = "select " +
            "la.* " +
            "from PurchaseRequest la  " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` budgetChecker on la.FK_budgetCheckedByUserId = budgetChecker.id " +
            "LEFT JOIN `User` reviewedAcceptedBy on la.FK_reviewedAcceptedByUserId = reviewedAcceptedBy.id  " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId NOT IN(:documentStatusIds) AND " +
            "(approveUser.id = :userId OR createUser.id = :userId OR budgetChecker.id = :userId OR reviewedAcceptedBy.id = :userId)",
            nativeQuery = true)
    List<PurchaseRequest> findByAllowedUserVoucherDateBetweenAndDocumentStatusIdNotIn(@Param("userId") Integer userId,
                                                                                      @Param("from") Date from,
                                                                                      @Param("to") Date to,
                                                                                      @Param("documentStatusIds") Collection<Integer> documentStatusIds);

    @Query(value = "select " +
            "la.* " +
            "from PurchaseRequest la  " +
            "LEFT JOIN `User` createUser on la.FK_createdByUserId = createUser.id " +
            "LEFT JOIN `User` approveUser on la.FK_approvedByUserId = approveUser.id " +
            "LEFT JOIN `User` inventoryCheckUser on la.FK_inventoryCheckedByUserId = inventoryCheckUser.id " +
            "LEFT JOIN `User` reviewedAcceptedBy on la.FK_reviewedAcceptedByUserId = reviewedAcceptedBy.id  " +
            "WHERE la.voucherDate BETWEEN :from AND :to AND FK_documentStatusId IN(:documentStatusId) " +
            "AND (createUser.id = :userId OR approveUser.id = :userId OR inventoryCheckUser.id = :userId OR reviewedAcceptedBy.id = :userId)",
            nativeQuery = true)
    List<PurchaseRequest> findByAllowedUserVoucherDateBetweenAndDocumentStatusId(@Param("userId") Integer userId,
                                                                                 @Param("from") Date from,
                                                                                 @Param("to") Date to,
                                                                                 @Param("documentStatusId") Integer documentStatusId);

    List<PurchaseRequest> findByDocumentStatusIdNotIn(List<Integer> integers);

    List<PurchaseRequest> findByDocumentStatusId(Integer status);
    List<PurchaseRequest> findByOrderByIdDesc();
    List<PurchaseRequest> findByBudgetLineItemDetailId(Integer budgetLineItemDetailID);

    @Transactional
    @Query(value = "select " +
            "rv.* " +
            "from PurchaseRequest rv " +
            "JOIN PurchaseRequestDetail ON rv.id = PurchaseRequestDetail.FK_purchaseRequestId " +
            "JOIN CanvassDetail ON PurchaseRequestDetail.id = CanvassDetail.FK_purchaseRequestDetailId " +
            "WHERE PurchaseRequestDetail.id not in (select PoDetail.FK_purchaseRequestDetailId from PoDetail JOIN PurchaseOrder ON PoDetail.FK_purchaseOrderId = PurchaseOrder.id AND PurchaseOrder.FK_documentStatusId != 26 ) " +
            "AND PurchaseRequestDetail.id not in (select QuotationItem.FK_purchaseRequestDetailId FROM QuotationItem JOIN Quotation ON QuotationItem.FK_quotationId = Quotation.id AND Quotation.FK_documentStatusId != 26) " +
            "AND PurchaseRequestDetail.id not in (select JoDetail.FK_purchaseRequestDetailId from JoDetail JOIN JobOrder ON JoDetail.FK_jobOrderId = JobOrder.id AND JobOrder.FK_documentStatusId != 26) " +
            "GROUP BY rv.id  ORDER BY rv.id DESC", nativeQuery = true)
    List<PurchaseRequest> findPurchaseRequestsWithQuotations();

    PurchaseRequest findPurchaseRequestByCode(String code);

    @Query(value = "SELECT " +
            "CONCAT_WS(', ', GROUP_CONCAT(DISTINCT rv.code ORDER BY rv.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT c.code ORDER BY c.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT q.code ORDER BY q.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT po.code ORDER BY po.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT rr.code ORDER BY rr.code ASC SEPARATOR ', ')) AS numbers, " +
            "   rv.id as id, " +
            "   rv.code, " +
            "   rv.voucherDate, " +
            "   rv.purpose, " +
            "   u.fullName as fullname, " +
            "   rr.id as rrId, " +
            "   rr.code as rrCode, " +
            "   rr.totalQuantity as quantityReceived " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN PurchaseRequestDetail rd ON rv.id = rd.FK_PurchaseRequestId " +
            "LEFT JOIN CanvassDetail cd ON rd.id = cd.FK_purchaseRequestDetailId " +
            "LEFT JOIN Canvass c ON cd.FK_canvassId = c.id " +
            "LEFT JOIN QuotationDetail qd ON rd.id = qd.FK_purchaseRequestDetailId " +
            "LEFT JOIN Quotation q ON qd.FK_quotationId = q.id " +
            "LEFT JOIN PoDetail pd ON pd.FK_purchaseRequestDetailId = rd.id " +
            "LEFT JOIN purchaseorder po ON pd.FK_purchaseOrderId = po.id " +
            "INNER JOIN ReceivingReportDetail rrd ON rd.id = rrd.FK_purchaseRequestDetailId " +
            "INNER JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
            "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
            "WHERE IF(pd.FK_purchaseRequestDetailId IS NULL, true, " +
            "rv.id IN (SELECT rd.FK_PurchaseRequestId " +
            "FROM PurchaseRequestDetail rd " +
            "INNER JOIN PoDetail pd ON rd.id = pd.FK_purchaseRequestDetailId " +
            "GROUP BY rd.id " +
            "HAVING SUM(pd.deliveredQuantity) > SUM(rd.withdrawQuantity) OR (pd.id IS NULL AND rd.withdrawquantity<rd.quantity)) " +
            ") AND rr.FK_inventoryLocationId = :invLocId AND rv.FK_createdByUserId = :userId " +
            "GROUP BY rv.id " +
            "ORDER BY rv.id ",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseRequest rv " +
                    "INNER JOIN PurchaseRequestDetail rd ON rv.id = rd.FK_PurchaseRequestId " +
                    "LEFT JOIN CanvassDetail cd ON rd.id = cd.FK_purchaseRequestDetailId " +
                    "LEFT JOIN Canvass c ON cd.FK_canvassId = c.id " +
                    "LEFT JOIN QuotationDetail qd ON rd.id = qd.FK_purchaseRequestDetailId " +
                    "LEFT JOIN Quotation q ON qd.FK_quotationId = q.id " +
                    "LEFT JOIN PoDetail pd ON pd.FK_purchaseRequestDetailId = rd.id " +
                    "LEFT JOIN purchaseorder po ON pd.FK_purchaseOrderId = po.id " +
                    "INNER JOIN ReceivingReportDetail rrd ON rd.id = rrd.FK_purchaseRequestDetailId " +

                    "INNER JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
                    "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
                    "WHERE IF(pd.FK_purchaseRequestDetailId IS NULL, true, " +
                    "rv.id IN (SELECT rd.FK_PurchaseRequestId " +
                    "FROM PurchaseRequestDetail rd " +
                    "INNER JOIN PoDetail pd ON rd.id = pd.FK_purchaseRequestDetailId " +
                    "GROUP BY rd.id " +
                    "HAVING SUM(pd.deliveredQuantity) > SUM(rd.withdrawQuantity) OR (pd.id IS NULL AND rd.withdrawquantity<rd.quantity)) " +
                    ") AND rr.FK_inventoryLocationId = :invLocId AND rv.FK_createdByUserId = :userId " +
                    "GROUP BY rv.id " +
                    "ORDER BY rv.id ",
            nativeQuery = true)
    Page<Map<String, Object>> findPurchaseRequestsForStockWithdrawal(@Param("invLocId") Integer invLocId, @Param("userId") Integer userId, Pageable pageable);

    @Query(value = "SELECT " +
            "CONCAT_WS(', ', GROUP_CONCAT(DISTINCT rv.code ORDER BY rv.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT c.code ORDER BY c.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT q.code ORDER BY q.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT po.code ORDER BY po.code ASC SEPARATOR ', '), " +
            "GROUP_CONCAT(DISTINCT rr.code ORDER BY rr.code ASC SEPARATOR ', ')) AS numbers, " +
            "rv.id, rv.code, rv.voucherDate, rv.purpose, u.fullName, " +
            "rr.id as rrId, rr.code as rrCode, rr.totalQuantity as quantityReceived " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN PurchaseRequestDetail rd ON rv.id = rd.FK_PurchaseRequestId " +
            "LEFT JOIN CanvassDetail cd ON rd.id = cd.FK_purchaseRequestDetailId " +
            "LEFT JOIN Canvass c ON cd.FK_canvassId = c.id " +
            "LEFT JOIN QuotationDetail qd ON rd.id = qd.FK_purchaseRequestDetailId " +
            "LEFT JOIN Quotation q ON qd.FK_quotationId = q.id " +
            "LEFT JOIN PoDetail pd ON pd.FK_purchaseRequestDetailId = rd.id " +
            "LEFT JOIN purchaseorder po ON pd.FK_purchaseOrderId = po.id " +
            "INNER JOIN ReceivingReportDetail rrd ON rd.id = rrd.FK_purchaseRequestDetailId " +
            "INNER JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
            "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
            "WHERE IF(pd.FK_purchaseRequestDetailId IS NULL, true, " +
            "rv.id IN (SELECT rd.FK_PurchaseRequestId " +
            "FROM PurchaseRequestDetail rd " +
            "INNER JOIN PoDetail pd ON rd.id = pd.FK_purchaseRequestDetailId " +
            "GROUP BY rd.id " +
            "HAVING SUM(pd.deliveredQuantity) > SUM(rd.withdrawQuantity) OR (pd.id IS NULL AND rd.withdrawquantity<rd.quantity)) " +
            ") AND rr.FK_inventoryLocationId = :invLocId AND rv.FK_createdByUserId = :userId " +
            "AND (rv.code LIKE :query OR rv.purpose LIKE :query OR u.fullName LIKE :query) " +
            "GROUP BY rv.id " +
            "ORDER BY rv.id \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseRequest rv " +
                    "INNER JOIN PurchaseRequestDetail rd ON rv.id = rd.FK_PurchaseRequestId " +
                    "LEFT JOIN CanvassDetail cd ON rd.id = cd.FK_purchaseRequestDetailId " +
                    "LEFT JOIN Canvass c ON cd.FK_canvassId = c.id " +
                    "LEFT JOIN QuotationDetail qd ON rd.id = qd.FK_purchaseRequestDetailId " +
                    "LEFT JOIN Quotation q ON qd.FK_quotationId = q.id " +
                    "LEFT JOIN PoDetail pd ON pd.FK_purchaseRequestDetailId = rd.id " +
                    "LEFT JOIN purchaseorder po ON pd.FK_purchaseOrderId = po.id " +
                    "INNER JOIN ReceivingReportDetail rrd ON rd.id = rrd.FK_purchaseRequestDetailId " +
                    "INNER JOIN ReceivingReport rr ON rr.id = rrd.FK_receivingReportId " +
                    "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
                    "WHERE IF(pd.FK_purchaseRequestDetailId IS NULL, true, " +
                    "rv.id IN (SELECT rd.FK_PurchaseRequestId " +
                    "FROM PurchaseRequestDetail rd " +
                    "INNER JOIN PoDetail pd ON rd.id = pd.FK_purchaseRequestDetailId " +
                    "GROUP BY rd.id " +
                    "HAVING SUM(pd.deliveredQuantity) > SUM(rd.withdrawQuantity) OR (pd.id IS NULL AND rd.withdrawquantity<rd.quantity)) " +
                    ") AND rr.FK_inventoryLocationId = :invLocId AND rv.FK_createdByUserId = :userId " +
                    "AND (rv.code LIKE :query OR rv.purpose LIKE :query OR u.fullName LIKE :query) " +
                    "GROUP BY rv.id " +
                    "ORDER BY rv.id",
            nativeQuery = true)
    Page<Map<String, Object>> findPurchaseRequestsForStockWithdrawal(@Param("query") String query,
                                                                     @Param("invLocId") Integer invLocId,
                                                                     @Param("userId") Integer userId,
                                                                     Pageable pageable);

    @Query(value = "SELECT " +
            "rv.id, rv.code, rv.voucherDate, rv.purpose, u.fullName " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = rv.id " +
            "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
            "WHERE rv.FK_documentStatusId = 7 " +
            "GROUP BY rv.id " +
            "HAVING SUM(rvd.poQuantity) = 0 AND SUM(rvd.Quantity) != SUM(rvd.rrQuantity) \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseRequest rv " +
                    "INNER JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = rv.id " +
                    "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
                    "WHERE rv.FK_documentStatusId = 7 " +
                    "GROUP BY rv.id " +
                    "HAVING SUM(rvd.poQuantity) = 0 AND SUM(rvd.Quantity) != SUM(rvd.rrQuantity)",
            nativeQuery = true)
    Page<Object[]> findPurchaseRequestsForRR(Pageable pageable);

    @Query(value = "SELECT " +
            "rv.id, rv.code, rv.voucherDate, rv.purpose, u.fullName " +
            "FROM PurchaseRequest rv " +
            "INNER JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = rv.id " +
            "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
            "WHERE rv.FK_documentStatusId = 7 " +
            "AND (rv.code LIKE :query OR rv.purpose LIKE :query OR u.fullName LIKE :query) " +
            "GROUP BY rv.id " +
            "HAVING SUM(rvd.poQuantity) = 0 AND SUM(rvd.Quantity) != SUM(rvd.rrQuantity) \n#pageable\n",
            countQuery = "SELECT COUNT(*) " +
                    "FROM PurchaseRequest rv " +
                    "INNER JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = rv.id " +
                    "INNER JOIN User u ON rv.FK_createdByUserId = u.id " +
                    "WHERE rv.FK_documentStatusId = 7 " +
                    "AND (rv.code LIKE :query OR rv.purpose LIKE :query OR u.fullName LIKE :query) " +
                    "GROUP BY rv.id " +
                    "HAVING SUM(rvd.poQuantity) = 0 AND SUM(rvd.Quantity) != SUM(rvd.rrQuantity)",
            nativeQuery = true)
    Page<Object[]> findPurchaseRequestsForRR(@Param("query") String query,
                                             Pageable pageable);

    @Transactional
    @Query(value = "SELECT pr.* FROM PurchaseRequest pr " +
            "LEFT JOIN PurchaseRequestDetail prd ON prd.FK_purchaseRequestId = pr.id " +
            "WHERE (prd.quantity - prd.poQuantity) > 0 " +
            "GROUP BY pr.id ", nativeQuery = true)
    List<PurchaseRequest> findPurchaseRequestsForPoAmountBudgetBalance();

    @Transactional
    @Query(value = "SELECT " +
            "* " +
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
            "GROUP BY pr.id " +
            "ORDER BY pr.code ", nativeQuery = true)
    List<PurchaseRequest> findAllPurchaseRequestForCanvass(@Param("rvTypes") List rvTypes,
                                                           @Param("approvedStatusId")Integer approvedStatusId,
                                                           @Param("cancelledStatusId")Integer cancelledStatusId);

    List<PurchaseRequest> findAllByVoucherDateBetweenAndDocumentStatusIdOrderByCode(Date from, Date to, Integer statusId);
    List<PurchaseRequest> findAllByVoucherDateBetweenAndDocumentStatusIdNotInOrderByCode(Date from, Date to, List<Integer> statusIds);

    Page<PurchaseRequest> findByCodeContainingIgnoreCase(String code, Pageable pageable);

}
