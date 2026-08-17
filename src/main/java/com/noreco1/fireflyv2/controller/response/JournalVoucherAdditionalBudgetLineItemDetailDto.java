package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.BudgetDetail;
import com.noreco1.fireflyv2.model.BudgetLineItemDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 11/06/2026.
 */
public class JournalVoucherAdditionalBudgetLineItemDetailDto {

    private Integer transactionId;
    private List<BudgetDetail> budgetDetails = new ArrayList<>();
    private List<BudgetLineItemDetail> budgetLineItemDetails = new ArrayList<>();

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public List<BudgetDetail> getBudgetDetails() {
        return budgetDetails;
    }

    public void setBudgetDetails(List<BudgetDetail> budgetDetails) {
        this.budgetDetails = budgetDetails;
    }

    public List<BudgetLineItemDetail> getBudgetLineItemDetails() {
        return budgetLineItemDetails;
    }

    public void setBudgetLineItemDetails(List<BudgetLineItemDetail> budgetLineItemDetails) {
        this.budgetLineItemDetails = budgetLineItemDetails;
    }
}
