package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin on 7/14/2015.
 */
public class CommonRegisterDetail {

    private String reference;
    private Date voucherDate;
    private String code;
    private String title;
    private String explanation;
    private BigDecimal debit;
    private BigDecimal credit;
    private String docNumber;
    private Date vDate;
    private String payee;
    private String sDebit;
    private String sCredit;

    private String reportType;

    public CommonRegisterDetail() {}

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public Date getvDate() {
        return vDate;
    }

    public void setvDate(Date vDate) {
        this.vDate = vDate;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getsDebit() {
        return sDebit;
    }

    public void setsDebit(String sDebit) {
        this.sDebit = sDebit;
    }

    public String getsCredit() {
        return sCredit;
    }

    public void setsCredit(String sCredit) {
        this.sCredit = sCredit;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

}
