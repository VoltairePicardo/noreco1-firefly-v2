package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin.
 */
public class PCVDetail {
    private String code;
    private String accountNumber;
    private String accountCode;
    private String payee;
    private String natureOfPayment;
    private String title;
    private String remarks;
    private BigDecimal amount;
    private Date voucherDate;
    private Integer documentStatus;
    private Integer batchId;
    private Integer count;
    private BigDecimal totalAmount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(String natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public Integer getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(Integer documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
