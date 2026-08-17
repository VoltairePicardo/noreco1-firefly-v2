package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.BudgetSubItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetSubItemRepo extends JpaRepository<BudgetSubItem, Integer> {

    List<BudgetSubItem> findAllByBudgetLineItemDetailId(Integer budgetLineItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(totalAmount), 0) FROM ( " +
            "  SELECT bsi.amount AS totalAmount FROM BudgetSubItem bsi WHERE bsi.id = 1 " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(cv.amount*-1) AS totalAmount FROM CheckVoucher cv " +
            "  JOIN BudgetSubItem blid ON cv.FK_budgetSubItemId = blid.id " +
            "  WHERE cv.FK_documentStatusId != :documentStatusId AND blid.id = :budgetLineSubItemDetailId " +
            ") AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetSubItemAmountBalanceCV(@Param("documentStatusId") Integer documentStatusId,
                                               @Param("budgetLineSubItemDetailId") Integer budgetLineSubItemDetailId);

    @Query(value = "SELECT COALESCE(SUM(totalAmount), 0) FROM (  " +
            "  SELECT bsi.amount AS totalAmount FROM BudgetSubItem bsi WHERE bsi.id = :budgetLineSubItemDetailId  " +
            "   " +
            "  UNION ALL " +
            "   " +
            "  SELECT SUM(pod.amount*-1) AS totalAmount FROM PurchaseOrder po " +
            "  INNER JOIN PoDetail pod ON pod.FK_purchaseOrderId = po.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = pod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE po.FK_documentStatusId != :documentStatusId AND pr.FK_budgetSubItemId = :budgetLineSubItemDetailId " +
            "  " +
            "  UNION ALL " +
            "  " +
            "  SELECT SUM(jod.amount*-1) AS totalAmount FROM JobOrder jo " +
            "  INNER JOIN JoDetail jod ON jod.FK_jobOrderId = jo.id " +
            "  INNER JOIN PurchaseRequestDetail prd ON prd.id = jod.FK_purchaseRequestDetailId " +
            "  INNER JOIN PurchaseRequest pr ON pr.id = prd.FK_purchaseRequestId " +
            "  WHERE jo.FK_documentStatusId != :documentStatusId AND pr.FK_budgetSubItemId = :budgetLineSubItemDetailId " +
            "  ) AS budgetLineItemDetailAmountBalance ", nativeQuery = true)
    BigDecimal getBudgetSubItemAmountBalancePOJO(@Param("documentStatusId") Integer documentStatusId,
                                                 @Param("budgetLineSubItemDetailId") Integer budgetLineSubItemDetailId);

}
