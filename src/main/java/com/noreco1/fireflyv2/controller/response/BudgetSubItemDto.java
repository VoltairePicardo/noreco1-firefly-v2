package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.BudgetLineItemDetail;
import com.noreco1.fireflyv2.model.BudgetSubItem;

import java.util.ArrayList;
import java.util.List;

public class BudgetSubItemDto {

    private BudgetLineItemDetail budgetLineItemDetail;
    private List<BudgetSubItem> budgetSubItems = new ArrayList<>();

    public BudgetLineItemDetail getBudgetLineItemDetail() {
        return budgetLineItemDetail;
    }

    public void setBudgetLineItemDetail(BudgetLineItemDetail budgetLineItemDetail) {
        this.budgetLineItemDetail = budgetLineItemDetail;
    }

    public List<BudgetSubItem> getBudgetSubItems() {
        return budgetSubItems;
    }

    public void setBudgetSubItems(List<BudgetSubItem> budgetSubItems) {
        this.budgetSubItems = budgetSubItems;
    }
}
