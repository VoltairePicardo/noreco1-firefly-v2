package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.util.Date;

public class CreditCardPurchaseRequestDto {

    private Integer id;
    private String code;
    private Date voucherDate;
    private DocumentStatus documentStatus;
    private Workflow workflow;
    private Transaction transaction;
    private SlEntity createdBy;
    private SlEntity requestedBy;
    private SlEntity supplier;
    private String purpose;
    private Mode mode;
    private FundingSource fundingSource;
    private SlEntity recommendingOfficer;
    private SlEntity approvingOfficer;
    private Date lastUpdated;
    private PurchaseOrder purchaseOrder;
    private JobOrder jobOrder;
    private CreditCardPurchaseRequestBatch batch;
    private Account expenseAccount;

    private Boolean forAccountingAdditionalDetails;

    public CreditCardPurchaseRequestDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public SlEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(SlEntity createdBy) {
        this.createdBy = createdBy;
    }

    public SlEntity getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(SlEntity requestedBy) {
        this.requestedBy = requestedBy;
    }

    public SlEntity getSupplier() {
        return supplier;
    }

    public void setSupplier(SlEntity supplier) {
        this.supplier = supplier;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public FundingSource getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    public SlEntity getRecommendingOfficer() {
        return recommendingOfficer;
    }

    public void setRecommendingOfficer(SlEntity recommendingOfficer) {
        this.recommendingOfficer = recommendingOfficer;
    }

    public SlEntity getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(SlEntity approvingOfficer) {
        this.approvingOfficer = approvingOfficer;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public JobOrder getJobOrder() {
        return jobOrder;
    }

    public void setJobOrder(JobOrder jobOrder) {
        this.jobOrder = jobOrder;
    }

    public CreditCardPurchaseRequestBatch getBatch() {
        return batch;
    }

    public void setBatch(CreditCardPurchaseRequestBatch batch) {
        this.batch = batch;
    }

    public Account getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(Account expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public Boolean getForAccountingAdditionalDetails() {
        return forAccountingAdditionalDetails;
    }

    public void setForAccountingAdditionalDetails(Boolean forAccountingAdditionalDetails) {
        this.forAccountingAdditionalDetails = forAccountingAdditionalDetails;
    }
}
