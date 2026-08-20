package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetLineItem;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetLineItemDetailRepo extends JpaRepository<BudgetLineItemDetail, Integer> {

    List<BudgetLineItemDetail> findAllByBudgetLineItemId(Integer id);

    @Transactional
    Long deleteByBudgetLineItemId(Integer transId);

    List<BudgetLineItemDetail> findAllByBudgetLineItemDocumentStatusIdOrderByCodeAsc(Integer status);
    List<BudgetLineItemDetail> findAllByBudgetLineItemDocumentStatusIdAndBudgetLineItemDivisionIdOrderByCodeAsc(Integer status, Integer division);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailBalance.quantity), 0) FROM ( " +
            "  SELECT blid.quantity AS quantity FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId " +
            "  " +
            "  UNION  " +
            "  " +
            "  SELECT SUM(rvd.quantity*-1) AS quantity FROM PurchaseRequest rv " +
            "  JOIN PurchaseRequestDetail rvd ON rvd.FK_PurchaseRequestId = rv.id " +
            "  WHERE rv.FK_documentStatusId = :documentStatusId AND  rv.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            ") AS budgetLineItemDetailBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailQuantityBalance(@Param("documentStatusId") Integer documentStatusId,
                                                      @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "  SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(pod.amount*-1) AS totalAmount FROM PurchaseOrder po " +
            "  INNER JOIN PoDetail pod ON pod.FK_purchaseOrderId = po.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = pod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE po.FK_documentStatusId = :documentStatusId AND pr.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(jod.amount*-1) AS totalAmount FROM JobOrder jo " +
            "  INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = jod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE jo.FK_documentStatusId = :documentStatusId AND pr.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(prd.amount*-1) AS totalAmount FROM PaymentRequest pr " +
            "  INNER JOIN PaymentRequestDetail prd ON prd.FK_paymentRequestId = pr.id " +
            "  WHERE pr.FK_documentStatusId = :documentStatusId AND pr.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(ca.amount*-1) AS totalAmount FROM CashAdvance ca " +
            "  WHERE ca.FK_documentStatusId = :documentStatusId AND ca.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            " " +
            " UNION ALL " +
            " " +
            "  SELECT SUM(pct.amount*-1) AS totalAmount FROM PettyCashTrans pct " +
            "JOIN budgetlineitemdetail blid ON pct.FK_budgetLineItemDetailId = blid.id " +
            "LEFT JOIN PettyCashLiquidation pcl ON pct.id = pcl.FK_pettyCashTransId " +
            "WHERE pcl.id IS NOT NULL AND pct.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId  " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalance(@Param("documentStatusId") Integer documentStatusId,
                                                    @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "  SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(cv.amount*-1) AS totalAmount FROM CheckVoucher cv " +
            "JOIN budgetlineitemdetail blid ON cv.FK_budgetLineItemDetailId = blid.id " +
            "WHERE cv.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalanceCV(@Param("documentStatusId") Integer documentStatusId,
                                                      @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "  SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(cv.amount*-1) AS totalAmount FROM CheckVoucher cv " +
            "JOIN budgetlineitemdetail blid ON cv.FK_budgetLineItemDetailId = blid.id " +
            "WHERE cv.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId " +
            " " +
            " UNION ALL " +
            " " +
            " SELECT SUM(ca.amount*-1) AS totalAmount FROM CashAdvance ca " +
            "JOIN BudgetLineItemDetail blid ON ca.FK_budgetLineItemDetailId = blid.id " +
            "WHERE ca.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalanceCA(@Param("documentStatusId") Integer documentStatusId,
                                                      @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            " " +
            "UNION ALL " +
            " " +
            "SELECT SUM(pct.amount*-1) AS totalAmount FROM PettyCashTrans pct " +
            "JOIN budgetlineitemdetail blid ON pct.FK_budgetLineItemDetailId = blid.id " +
            "LEFT JOIN PettyCashLiquidation pcl ON pct.id = pcl.FK_pettyCashTransId " +
            "WHERE pcl.id IS NULL AND pct.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId " +
            " " +
            " UNION ALL " +
            " " +
            "  SELECT SUM(pct.amount*-1) AS totalAmount FROM PettyCashTrans pct " +
            "JOIN budgetlineitemdetail blid ON pct.FK_budgetLineItemDetailId = blid.id " +
            "LEFT JOIN PettyCashLiquidation pcl ON pct.id = pcl.FK_pettyCashTransId " +
            "WHERE pcl.id IS NOT NULL AND pct.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId  " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalancePCL(@Param("documentStatusId") Integer documentStatusId,
                                                       @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "  SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(pod.amount*-1) AS totalAmount FROM PurchaseOrder po " +
            "  INNER JOIN PoDetail pod ON pod.FK_purchaseOrderId = po.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = pod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE po.FK_documentStatusId != :documentStatusId AND pr.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(jod.amount*-1) AS totalAmount FROM JobOrder jo " +
            "  INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = jod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE jo.FK_documentStatusId != :documentStatusId AND pr.FK_budgetLineItemDetailId = :budgetLineItemDetailId " +
            "  ) AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalancePOJO(@Param("documentStatusId") Integer documentStatusId,
                                                        @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(budgetLineItemDetailAmountBalance.totalAmount), 0) FROM (  " +
            "  SELECT blid.totalPrice AS totalAmount FROM BudgetLineItemDetail blid WHERE blid.id = :budgetLineItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(cv.amount*-1) AS totalAmount FROM CheckVoucher cv " +
            "JOIN budgetlineitemdetail blid ON cv.FK_budgetLineItemDetailId = blid.id " +
            " " +
            "UNION ALL " +
            " " +
            "SELECT SUM(pct.amount*-1) AS totalAmount FROM PettyCashTrans pct " +
            "JOIN budgetlineitemdetail blid ON pct.FK_budgetLineItemDetailId = blid.id " +
            "LEFT JOIN PettyCashLiquidation pcl ON pct.id = pcl.FK_pettyCashTransId " +
            "WHERE pcl.id IS NULL AND pct.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineItemDetailId " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetLineItemDetailAmountBalancePCV(@Param("documentStatusId") Integer documentStatusId,
                                                       @Param("budgetLineItemDetailId") Integer budgetLineItemDetailId);

    @Query(value = "SELECT DISTINCT blid.* " +
            "FROM BudgetLineItemDetail blid " +
            "INNER JOIN BudgetLineItem bli ON bli.id = blid.FK_budgetLineItemId " +
            "LEFT JOIN BudgetSubItem bsi ON bsi.FK_budgetLineItemDetailId = blid.id " +
            "WHERE (:yearParam IS NULL OR bli.`year` = :yearParam) " +
            "  AND (:divisionParam IS NULL OR bli.FK_divisionId = :divisionParam) " +
            "  AND (:searchText IS NULL OR blid.title LIKE :searchText OR bsi.description LIKE :searchText)" +
            "  ORDER BY blid.title ", nativeQuery = true)
    List<BudgetLineItemDetail> getAllBudgetLineItemByParam(@Param("yearParam") Integer yearParam,
                                                           @Param("divisionParam") Integer divisionParam,
                                                           @Param("searchText") String searchText);

    @Query(value = "SELECT COALESCE(e.code, '') " +
            "FROM BudgetLineItemDetail e " +
            "WHERE e.`year` = :yearParam " +
            "  AND e.code LIKE CONCAT(:department, '-', :division, '-', :yearParam, '-%') " +
            "ORDER BY e.id DESC " +
            "LIMIT 1 ", nativeQuery = true)
    String findLatestBudgetLineItemCodeByDepartmentAndDivisionParams(@Param("yearParam") Integer yearParam,
                                                                     @Param("department") String department,
                                                                     @Param("division") String division);

    @Query(value = "SELECT COALESCE(e.code, '') " +
            "FROM BudgetLineItemDetail e " +
            "WHERE e.`year` = :yearParam " +
            "  AND e.code LIKE CONCAT(:department, '-', :yearParam, '-%') " +
            "ORDER BY e.id DESC " +
            "LIMIT 1 ", nativeQuery = true)
    String findLatestBudgetLineItemCodeByDepartmentParams(@Param("yearParam") Integer yearParam,
                                                          @Param("department") String department);

    @Query(value = "SELECT e.code FROM BudgetLineItemDetail e " +
            "WHERE e.year = :yearParam " +
            "AND ( " +
            "    (:division IS NOT NULL AND e.code LIKE CONCAT(:department, '-', :division, '-', :yearParam, '-%')) " +
            " OR (:division IS NULL AND e.code LIKE CONCAT(:department, '-', :yearParam, '-%')) " +
            ") " +
            "ORDER BY e.id DESC LIMIT 1", nativeQuery = true)
    String findLatestBudgetLineItemCodeDynamic(@Param("yearParam") Integer year, @Param("department") String department, @Param("division") String division);

}
