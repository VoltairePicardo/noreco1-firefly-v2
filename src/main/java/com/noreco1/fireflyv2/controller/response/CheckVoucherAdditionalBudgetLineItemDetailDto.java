package com.noreco1.fireflyv2.controller.response;

/**
 * Created by User on 10/06/2026.
 */
public class CheckVoucherAdditionalBudgetLineItemDetailDto {

    private Integer transactionId;
    private Integer budgetLineItemDetailId;
    private Integer budgetDetailId;

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getBudgetLineItemDetailId() {
        return budgetLineItemDetailId;
    }

    public void setBudgetLineItemDetailId(Integer budgetLineItemDetailId) {
        this.budgetLineItemDetailId = budgetLineItemDetailId;
    }

    public Integer getBudgetDetailId() {
        return budgetDetailId;
    }

    public void setBudgetDetailId(Integer budgetDetailId) {
        this.budgetDetailId = budgetDetailId;
    }
}
