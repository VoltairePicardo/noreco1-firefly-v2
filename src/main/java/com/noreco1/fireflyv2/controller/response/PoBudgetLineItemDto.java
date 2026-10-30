package com.noreco1.fireflyv2.controller.response;

import jakarta.validation.constraints.NotNull;

public class PoBudgetLineItemDto {

    @NotNull
    private Integer transactionId;
    @NotNull
    private Integer budgetLineItemDetailId;

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
}
