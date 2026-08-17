package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 3/29/2016.
 */
public class CashFlowStatementDetail {

    private String account;
    private BigDecimal budget;
    private BigDecimal actualThisMonth;
    private BigDecimal toDate;
    private BigDecimal budgetBalance;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getActualThisMonth() {
        return actualThisMonth;
    }

    public void setActualThisMonth(BigDecimal actualThisMonth) {
        this.actualThisMonth = actualThisMonth;
    }

    public BigDecimal getToDate() {
        return toDate;
    }

    public void setToDate(BigDecimal toDate) {
        this.toDate = toDate;
    }

    public BigDecimal getBudgetBalance() {
        return budgetBalance;
    }

    public void setBudgetBalance(BigDecimal budgetBalance) {
        this.budgetBalance = budgetBalance;
    }
}
