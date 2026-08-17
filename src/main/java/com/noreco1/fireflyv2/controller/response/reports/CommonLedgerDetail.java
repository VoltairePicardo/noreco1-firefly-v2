package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;

/**
 * Created by TSI Admin on 5/11/2015.
 */
public class CommonLedgerDetail {

    private String accountTitle;
    private String accountCode;
    private BigDecimal glDebitAmount;
    private BigDecimal glCreditAmount;
    private BigDecimal slDebitAmount;
    private BigDecimal slCreditAmount;

    public CommonLedgerDetail() {}

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public BigDecimal getGlDebitAmount() {
        return glDebitAmount;
    }

    public void setGlDebitAmount(BigDecimal glDebitAmount) {
        this.glDebitAmount = glDebitAmount;
    }

    public BigDecimal getGlCreditAmount() {
        return glCreditAmount;
    }

    public void setGlCreditAmount(BigDecimal glCreditAmount) {
        this.glCreditAmount = glCreditAmount;
    }

    public BigDecimal getSlDebitAmount() {
        return slDebitAmount;
    }

    public void setSlDebitAmount(BigDecimal slDebitAmount) {
        this.slDebitAmount = slDebitAmount;
    }

    public BigDecimal getSlCreditAmount() {
        return slCreditAmount;
    }

    public void setSlCreditAmount(BigDecimal slCreditAmount) {
        this.slCreditAmount = slCreditAmount;
    }
}
