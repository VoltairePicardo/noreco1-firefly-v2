package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 5/15/2015.
 */
public class PoDto {
    private Integer id;
    private SlEntity vendor;
    private BigDecimal amount = BigDecimal.ZERO;
    private Date voucherDate;
    private String deliveryDate;
    private String localCode;
    private String deliveryTerm;
    private String deliveryAddress;
    private String deliveryTimeAndCompletion;
    private Integer paymentTerm;
    private SlEntity createdBy;
    private SlEntity checkedBy;
    private SlEntity budgetCheckedBy;
    private SlEntity approvedBy;
    private Integer transId;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private String deliveryTermPretty;
    private String purpose;
    private Vehicle vehicle;
    private CashAdvance cashAdvance;
    private PurchaseRequest purchaseRequest;
    private Boolean useCreditCard;
    private BigDecimal budgetLineItemBalancePOJORFP;
    private BigDecimal budgetLineItemBalanceCV;
    private BigDecimal cashFlowItemBalancePOJORFP;
    private BigDecimal cashFlowItemBalanceCV;
    private BigDecimal cashFlowItemTotal;
    private Date receivedDate;
    private String receivedBy;
    private Date expectedDeliveryDate;

    public PoDto() {}

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

    public SlEntity getBudgetCheckedBy() {
        return budgetCheckedBy;
    }

    public void setBudgetCheckedBy(SlEntity budgetCheckedBy) {
        this.budgetCheckedBy = budgetCheckedBy;
    }

    public SlEntity getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(SlEntity checkedBy) {
        this.checkedBy = checkedBy;
    }

    public String getDeliveryTerm() {
        return deliveryTerm;
    }

    public void setDeliveryTerm(String deliveryTerm) {
        this.deliveryTerm = deliveryTerm;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Integer getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(Integer paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public String getDeliveryTermPretty() {
        return deliveryTermPretty;
    }

    public void setDeliveryTermPretty(String deliveryTermPretty) {
        this.deliveryTermPretty = deliveryTermPretty;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public CashAdvance getCashAdvance() {
        return cashAdvance;
    }

    public void setCashAdvance(CashAdvance cashAdvance) {
        this.cashAdvance = cashAdvance;
    }

    public PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    public void setPurchaseRequest(PurchaseRequest purchaseRequest) {
        this.purchaseRequest = purchaseRequest;
    }

    public Boolean getUseCreditCard() {
        return useCreditCard;
    }

    public void setUseCreditCard(Boolean useCreditCard) {
        this.useCreditCard = useCreditCard;
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

    public String getDeliveryTimeAndCompletion() {
        return deliveryTimeAndCompletion;
    }

    public void setDeliveryTimeAndCompletion(String deliveryTimeAndCompletion) {
        this.deliveryTimeAndCompletion = deliveryTimeAndCompletion;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Date getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
}
