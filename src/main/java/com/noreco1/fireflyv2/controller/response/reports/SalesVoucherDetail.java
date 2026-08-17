package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 5/11/2015.
 */
public class SalesVoucherDetail {

    private String particulars;
    private String account;
    private BigDecimal amount;
    private BigDecimal debit;
    private BigDecimal credit;

    public SalesVoucherDetail() {}

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }
}
