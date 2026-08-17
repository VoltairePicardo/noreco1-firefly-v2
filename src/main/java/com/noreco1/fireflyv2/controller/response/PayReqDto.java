package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 7/21/2015.
 */
public class PayReqDto {
    private Integer id;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date voucherDate;
    private String localCode;
    private SlEntity createdBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private List<PaymentRequestDetail> paymentRequestDetails;
    private SlEntity vendor;
    private List<PaymentRequestBudgetLineItemDetail> paymentRequestBudgetLineItemDetails;
    private Date invoiceDate;
    private String invoiceNumber;
    private Date dueDate;
    private BigDecimal budgetAmountBalancePOJORFP;
    private BigDecimal budgetAmountBalanceCV;

    private BigDecimal cashFlowAmountBalancePOJORFP;
    private BigDecimal cashFlowAmountBalanceCV;

    public PayReqDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public SlEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SlEntity createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<PaymentRequestDetail> getPaymentRequestDetails() {
        return paymentRequestDetails;
    }

    public void setPaymentRequestDetails(List<PaymentRequestDetail> paymentRequestDetails) {
        this.paymentRequestDetails = paymentRequestDetails;
    }

    public SlEntity getVendor() {
        return vendor;
    }

    public void setVendor(SlEntity vendor) {
        this.vendor = vendor;
    }

    public List<PaymentRequestBudgetLineItemDetail> getPaymentRequestBudgetLineItemDetails() {
        return paymentRequestBudgetLineItemDetails;
    }

    public void setPaymentRequestBudgetLineItemDetails(List<PaymentRequestBudgetLineItemDetail> paymentRequestBudgetLineItemDetails) {
        this.paymentRequestBudgetLineItemDetails = paymentRequestBudgetLineItemDetails;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getBudgetAmountBalancePOJORFP() {
        return budgetAmountBalancePOJORFP;
    }

    public void setBudgetAmountBalancePOJORFP(BigDecimal budgetAmountBalancePOJORFP) {
        this.budgetAmountBalancePOJORFP = budgetAmountBalancePOJORFP;
    }

    public BigDecimal getBudgetAmountBalanceCV() {
        return budgetAmountBalanceCV;
    }

    public void setBudgetAmountBalanceCV(BigDecimal budgetAmountBalanceCV) {
        this.budgetAmountBalanceCV = budgetAmountBalanceCV;
    }

    public BigDecimal getCashFlowAmountBalancePOJORFP() {
        return cashFlowAmountBalancePOJORFP;
    }

    public void setCashFlowAmountBalancePOJORFP(BigDecimal cashFlowAmountBalancePOJORFP) {
        this.cashFlowAmountBalancePOJORFP = cashFlowAmountBalancePOJORFP;
    }

    public BigDecimal getCashFlowAmountBalanceCV() {
        return cashFlowAmountBalanceCV;
    }

    public void setCashFlowAmountBalanceCV(BigDecimal cashFlowAmountBalanceCV) {
        this.cashFlowAmountBalanceCV = cashFlowAmountBalanceCV;
    }
}
