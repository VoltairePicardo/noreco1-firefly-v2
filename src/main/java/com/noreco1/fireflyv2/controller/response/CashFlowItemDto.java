package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CashFlowItemDto {

    private Integer transId;
    private BigDecimal cashFlowItemBalancePOJORFP;
    private BigDecimal cashFlowItemBalanceCV;
    private BigDecimal cashFlowItemTotal;
    private ArrayList<PurchaseOrderBudgetDetail> purchaseOrderBudgetDetails = new ArrayList<>();
    private ArrayList<JobOrderBudgetDetail> jobOrderBudgetDetails = new ArrayList<>();
    private ArrayList<CashAdvanceBudgetDetail> cashAdvanceBudgetDetails = new ArrayList<>();
    private ArrayList<PettyCashTransBudgetDetail> pettyCashTransBudgetDetails = new ArrayList<>();
    private ArrayList<BudgetSubItem> budgetSubItems = new ArrayList<>();

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public BigDecimal getCashFlowItemBalancePOJORFP() {
        return cashFlowItemBalancePOJORFP;
    }

    public void setCashFlowItemBalancePOJORFP(BigDecimal cashFlowItemBalancePOJORFP) {
        this.cashFlowItemBalancePOJORFP = cashFlowItemBalancePOJORFP;
    }

    public BigDecimal getCashFlowItemBalanceCV() {
        return cashFlowItemBalanceCV;
    }

    public void setCashFlowItemBalanceCV(BigDecimal cashFlowItemBalanceCV) {
        this.cashFlowItemBalanceCV = cashFlowItemBalanceCV;
    }

    public BigDecimal getCashFlowItemTotal() {
        return cashFlowItemTotal;
    }

    public void setCashFlowItemTotal(BigDecimal cashFlowItemTotal) {
        this.cashFlowItemTotal = cashFlowItemTotal;
    }

    public ArrayList<PurchaseOrderBudgetDetail> getPurchaseOrderBudgetDetails() {
        return purchaseOrderBudgetDetails;
    }

    public void setPurchaseOrderBudgetDetails(ArrayList<PurchaseOrderBudgetDetail> purchaseOrderBudgetDetails) {
        this.purchaseOrderBudgetDetails = purchaseOrderBudgetDetails;
    }

    public ArrayList<JobOrderBudgetDetail> getJobOrderBudgetDetails() {
        return jobOrderBudgetDetails;
    }

    public void setJobOrderBudgetDetails(ArrayList<JobOrderBudgetDetail> jobOrderBudgetDetails) {
        this.jobOrderBudgetDetails = jobOrderBudgetDetails;
    }

    public ArrayList<CashAdvanceBudgetDetail> getCashAdvanceBudgetDetails() {
        return cashAdvanceBudgetDetails;
    }

    public void setCashAdvanceBudgetDetails(ArrayList<CashAdvanceBudgetDetail> cashAdvanceBudgetDetails) {
        this.cashAdvanceBudgetDetails = cashAdvanceBudgetDetails;
    }

    public ArrayList<PettyCashTransBudgetDetail> getPettyCashTransBudgetDetails() {
        return pettyCashTransBudgetDetails;
    }

    public void setPettyCashTransBudgetDetails(ArrayList<PettyCashTransBudgetDetail> pettyCashTransBudgetDetails) {
        this.pettyCashTransBudgetDetails = pettyCashTransBudgetDetails;
    }

    public ArrayList<BudgetSubItem> getBudgetSubItems() {
        return budgetSubItems;
    }

    public void setBudgetSubItems(ArrayList<BudgetSubItem> budgetSubItems) {
        this.budgetSubItems = budgetSubItems;
    }
}
