package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Office;
import com.noreco1.fireflyv2.model.PurchaseRequest;
import com.noreco1.fireflyv2.model.SlEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 6/18/2015.
 */
public class JoDto {
    private Integer id;
    private SlEntity vendor;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date voucherDate;
    private String localCode;
    private Integer term;
    private SlEntity createdBy;
    private SlEntity budgetCheckedBy;
    private SlEntity checkedBy;
    private SlEntity notedBy;
    private SlEntity approvedBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private String description;
    private Integer paymentTerm;
    private String paymentTermInWords;
    private PurchaseRequest purchaseRequest;
    private BigDecimal budgetLineItemBalancePOJORFP;
    private BigDecimal budgetLineItemBalanceCV;
    private BigDecimal cashFlowItemBalancePOJORFP;
    private BigDecimal cashFlowItemBalanceCV;
    private BigDecimal cashFlowItemTotal;

    public JoDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public SlEntity getVendor() {
        return vendor;
    }

    public void setVendor(SlEntity vendor) {
        this.vendor = vendor;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public SlEntity getNotedBy() {
        return notedBy;
    }

    public void setNotedBy(SlEntity notedBy) {
        this.notedBy = notedBy;
    }

    public SlEntity getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(SlEntity checkedBy) {
        this.checkedBy = checkedBy;
    }

    public SlEntity getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(SlEntity approvedBy) {
        this.approvedBy = approvedBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(Integer paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public String getPaymentTermInWords() {
        return paymentTermInWords;
    }

    public void setPaymentTermInWords(String paymentTermInWords) {
        this.paymentTermInWords = paymentTermInWords;
    }

    public PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    public void setPurchaseRequest(PurchaseRequest purchaseRequest) {
        this.purchaseRequest = purchaseRequest;
    }

    public BigDecimal getBudgetLineItemBalancePOJORFP() {
        return budgetLineItemBalancePOJORFP;
    }

    public void setBudgetLineItemBalancePOJORFP(BigDecimal budgetLineItemBalancePOJORFP) {
        this.budgetLineItemBalancePOJORFP = budgetLineItemBalancePOJORFP;
    }

    public BigDecimal getBudgetLineItemBalanceCV() {
        return budgetLineItemBalanceCV;
    }

    public void setBudgetLineItemBalanceCV(BigDecimal budgetLineItemBalanceCV) {
        this.budgetLineItemBalanceCV = budgetLineItemBalanceCV;
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

    public SlEntity getBudgetCheckedBy() {
        return budgetCheckedBy;
    }

    public void setBudgetCheckedBy(SlEntity budgetCheckedBy) {
        this.budgetCheckedBy = budgetCheckedBy;
    }
}
