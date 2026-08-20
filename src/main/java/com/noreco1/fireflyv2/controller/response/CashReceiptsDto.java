package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.controller.response.reports.CommonLedgerDetail;
import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.Office;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by TSI Admin on 4/23/2015.
 */
public class CashReceiptsDto {
    private Integer id;
    private String particulars;
    private Date voucherDate;
    private String localCode;
    private User preparedBy;
    private SlEntity checker;
    private SlEntity recommendingOfficer;
    private SlEntity approvingOfficer;
    private SlEntity postedBy;
    private Integer transId;
    private BigDecimal amount;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;
    private Office office;
    private List<CommonLedgerDetail> journalEntries;


    public CashReceiptsDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public SlEntity getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(SlEntity postedBy) {
        this.postedBy = postedBy;
    }

    public User getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(User preparedBy) {
        this.preparedBy = preparedBy;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public SlEntity getRecommendingOfficer() {
        return recommendingOfficer;
    }

    public void setRecommendingOfficer(SlEntity recommendingOfficer) {
        this.recommendingOfficer = recommendingOfficer;
    }

    public List<CommonLedgerDetail> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<CommonLedgerDetail> journalEntries) {
        this.journalEntries = journalEntries;
    }
}
