package com.noreco1.fireflyv2.repo;

import com.noreco1.fireflyv2.model.PurchaseOrderBudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface PurchaseOrderBudgetDetailRepo extends JpaRepository<PurchaseOrderBudgetDetail, Integer> {

    @Transactional
    public Long deleteByPurchaseOrderId(Integer transId);

    @Transactional
    List<PurchaseOrderBudgetDetail> findAllByPurchaseOrderId(Integer poId);

    PurchaseOrderBudgetDetail findFirstByPurchaseOrderIdOrderByIdAsc(Integer poId);

    @Query(value = "SELECT   " +
            "COALESCE(SUM(cashFlowItemAmountBalance.totalAmount), 0)   " +
            "FROM (    " +
            "  SELECT BudgetDetail.amount AS totalAmount FROM BudgetDetail WHERE BudgetDetail.FK_cashflowItemId = :cashFlowItemId    " +
            "     " +
            "  UNION ALL   " +
            "     " +
            "  SELECT COALESCE(SUM(PurchaseOrderBudgetDetail.amount*-1), 0) AS totalAmount FROM PurchaseOrder   " +
            "  INNER JOIN PurchaseOrderBudgetDetail ON PurchaseOrderBudgetDetail.FK_purchaseOrderId = PurchaseOrder.id   " +
            "  WHERE PurchaseOrder.FK_documentStatusId != :documentStatusId AND PurchaseOrderBudgetDetail.FK_cashflowItemId = :cashFlowItemId  " +
            "    " +
            "  UNION ALL   " +
            "    " +
            "  SELECT COALESCE(SUM(JobOrderBudgetDetail.amount*-1), 0) AS totalAmount FROM JobOrder   " +
            "  INNER JOIN JobOrderBudgetDetail ON JobOrderBudgetDetail.FK_jobOrderId = JobOrder.id   " +
            "  WHERE JobOrder.FK_documentStatusId != :documentStatusId AND JobOrderBudgetDetail.FK_cashflowItemId = :cashFlowItemId  " +
            "    " +
            "  UNION ALL   " +
            "    " +
            "  SELECT COALESCE(SUM(PaymentRequestBudgetDetail.amount*-1), 0) AS totalAmount FROM PaymentRequest   " +
            "  INNER JOIN PaymentRequestBudgetDetail ON PaymentRequestBudgetDetail.FK_paymentRequestId = PaymentRequest.id   " +
            "  WHERE PaymentRequest.FK_documentStatusId != :documentStatusId AND PaymentRequestBudgetDetail.FK_cashflowItemId = :cashFlowItemId  " +
            "    " +
            "  UNION ALL   " +
            "    " +
            "  SELECT COALESCE(SUM(CashAdvanceBudgetDetail.amount*-1), 0) AS totalAmount FROM CashAdvance   " +
            "  INNER JOIN CashAdvanceBudgetDetail ON CashAdvanceBudgetDetail.FK_cashAdvanceId = CashAdvance.id   " +
            "  WHERE CashAdvance.FK_documentStatusId != :documentStatusId AND CashAdvanceBudgetDetail.FK_cashflowItemId = :cashFlowItemId  " +
            "    " +
            ") AS cashFlowItemAmountBalance ", nativeQuery = true)
    BigDecimal getCashFlowItemDetailAmountBalancePOJORFPCA(@Param("documentStatusId") Integer documentStatusId,
                                                         @Param("cashFlowItemId") Integer cashFlowItemId);

    @Query(value = "SELECT COALESCE(SUM(cashFlowItemAmountBalance.totalAmount), 0) FROM (    " +
            "  SELECT BudgetDetail.amount AS totalAmount FROM BudgetDetail WHERE BudgetDetail.FK_cashflowItemId = :cashFlowItemId    " +
            "     " +
            "  UNION ALL   " +
            "     " +
            "  SELECT COALESCE(SUM(CheckVoucherBudgetDetail.amount*-1), 0) AS totalAmount FROM CheckVoucher   " +
            "  JOIN CheckVoucherBudgetDetail ON CheckVoucherBudgetDetail.FK_checkVoucherId = CheckVoucher.id   " +
            "  WHERE CheckVoucher.FK_documentStatusId != :documentStatusId AND CheckVoucherBudgetDetail.FK_cashflowItemId = :cashFlowItemId    " +
            ") AS cashFlowItemAmountBalance  ", nativeQuery = true)
    BigDecimal getCashFlowItemDetailAmountBalanceCV(@Param("documentStatusId") Integer documentStatusId,
                                                    @Param("cashFlowItemId") Integer cashFlowItemId);

}
