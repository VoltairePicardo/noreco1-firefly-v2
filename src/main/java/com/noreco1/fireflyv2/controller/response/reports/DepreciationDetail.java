package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 5/11/2015.
 */
public class DepreciationDetail implements Cloneable {

    private String code;
    private String description;
    private BigDecimal value;
    private BigDecimal depreciationAmount;
    private String expenseAccount;
    private String accumDepAccount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getDepreciationAmount() {
        return depreciationAmount;
    }

    public void setDepreciationAmount(BigDecimal depreciationAmount) {
        this.depreciationAmount = depreciationAmount;
    }

    public String getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(String expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public String getAccumDepAccount() {
        return accumDepAccount;
    }

    public void setAccumDepAccount(String accumDepAccount) {
        this.accumDepAccount = accumDepAccount;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
