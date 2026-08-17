package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

public class MaterialSalvageTicketDocumentDto {

    private Integer id;
    private String localCode;
    private BigDecimal netAmount;
    private String particulars;
    private Date voucherDate;
    private String preparedBy;
    private Integer slentityAccountNo;
    private String slentityName;
    private Date invoiceDate;
    private Integer paymentTerm;
    private Date dueDate;
    private Integer transactionId;
    private BigDecimal quantity;

    public MaterialSalvageTicketDocumentDto() {
    }

    public MaterialSalvageTicketDocumentDto(Integer id, String localCode, BigDecimal netAmount, String particulars, Date voucherDate, String preparedBy, Integer slentityAccountNo, String slentityName, Date invoiceDate, Integer paymentTerm, Date dueDate, Integer transactionId, BigDecimal quantity) {
        this.id = id;
        this.localCode = localCode;
        this.netAmount = netAmount;
        this.particulars = particulars;
        this.voucherDate = voucherDate;
        this.preparedBy = preparedBy;
        this.slentityAccountNo = slentityAccountNo;
        this.slentityName = slentityName;
        this.invoiceDate = invoiceDate;
        this.paymentTerm = paymentTerm;
        this.dueDate = dueDate;
        this.transactionId = transactionId;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }

    public Integer getSlentityAccountNo() {
        return slentityAccountNo;
    }

    public void setSlentityAccountNo(Integer slentityAccountNo) {
        this.slentityAccountNo = slentityAccountNo;
    }

    public String getSlentityName() {
        return slentityName;
    }

    public void setSlentityName(String slentityName) {
        this.slentityName = slentityName;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(Integer paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

}
