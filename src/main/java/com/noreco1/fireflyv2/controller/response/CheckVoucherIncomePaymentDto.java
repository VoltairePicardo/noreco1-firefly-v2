package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.SlEntity;

import java.math.BigDecimal;

/**
 * Created by User on 12/19/2016.
 */
public class CheckVoucherIncomePaymentDto {
    private Integer rowCount;
    private String payee;
    private String tin;
    private BigDecimal amount;
    private BigDecimal baseAmount;
    private BigDecimal percentage;

    public CheckVoucherIncomePaymentDto() {
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
