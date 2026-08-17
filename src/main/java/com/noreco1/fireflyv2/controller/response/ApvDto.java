package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by TSI Admin on 4/23/2015.
 */
public class ApvDto {
    private Integer id;
    private SlEntity vendor;
    private String particulars;
    private Date voucherDate;
    private Date dueDate;
    private String localCode;
    private SlEntity checker;
    private SlEntity approvingOfficer;
    private SlEntity postedBy;
    private SlEntity recommendingOfficer;
    private SlEntity reviewer;
    private SlEntity createdBy;
    private Integer transId;
    private BigDecimal amount;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private Date invoiceDate;
    private Integer paymentTerm;
    private List<Map> receivingReports;
    private String unpaidRemark;
    private List<IEMOPBilling> iemopBillings;

    @Getter @Setter private Boolean forInstallment;
    @Getter @Setter private Integer numberOfPayments;
    @Getter @Setter private List<AccountsPayableVoucherInstallmentDetail> installmentDetails;

    public ApvDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SlEntity getVendor() {
        return vendor;
    }

    public void setVendor(SlEntity vendor) {
        this.vendor = vendor;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getLocalCode() {
        return localCode;
    }

    public void setLocalCode(String localCode) {
        this.localCode = localCode;
    }

    public SlEntity getChecker() {
        return checker;
    }

    public void setChecker(SlEntity checker) {
        this.checker = checker;
    }

    public SlEntity getApprovingOfficer() {
        return approvingOfficer;
    }

    public void setApprovingOfficer(SlEntity approvingOfficer) {
        this.approvingOfficer = approvingOfficer;
    }

    public SlEntity getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(SlEntity postedBy) {
        this.postedBy = postedBy;
    }

    public SlEntity getRecommendingOfficer() {
        return recommendingOfficer;
    }

    public void setRecommendingOfficer(SlEntity recommendingOfficer) {
        this.recommendingOfficer = recommendingOfficer;
    }

    public SlEntity getReviewer() {
        return reviewer;
    }

    public void setReviewer(SlEntity reviewer) {
        this.reviewer = reviewer;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public List<Map> getReceivingReports() {
        return receivingReports;
    }

    public void setReceivingReports(List<Map> receivingReports) {
        this.receivingReports = receivingReports;
    }

    public String getUnpaidRemark() {
        return unpaidRemark;
    }

    public void setUnpaidRemark(String unpaidRemark) {
        this.unpaidRemark = unpaidRemark;
    }

    public List<IEMOPBilling> getIemopBillings() {
        return iemopBillings;
    }

    public void setIemopBillings(List<IEMOPBilling> iemopBillings) {
        this.iemopBillings = iemopBillings;
    }
}
