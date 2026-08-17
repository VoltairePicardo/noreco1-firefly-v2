package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.DocumentStatus;
import com.noreco1.fireflyv2.model.SlEntity;
import com.noreco1.fireflyv2.model.User;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 1/26/2016.
 */
public class OtherAccountReceivableDto {
    private Integer id;
    private String particulars;
    private Date voucherDate;
    private String localCode;
    private SlEntity checker;
    private SlEntity approvingOfficer;
    private User preparedBy;
    private Integer transId;
    private BigDecimal amount;
    private DocumentStatus documentStatus;
    private Date lastUpdated;
    private Date created;

    public OtherAccountReceivableDto() {
    }

    public OtherAccountReceivableDto(Integer id, String particulars, Date voucherDate, String localCode, SlEntity checker, SlEntity approvingOfficer, Integer transId, BigDecimal amount, DocumentStatus documentStatus, Date lastUpdated, Date created) {
        this.id = id;
        this.particulars = particulars;
        this.voucherDate = voucherDate;
        this.localCode = localCode;
        this.checker = checker;
        this.approvingOfficer = approvingOfficer;
        this.transId = transId;
        this.amount = amount;
        this.documentStatus = documentStatus;
        this.lastUpdated = lastUpdated;
        this.created = created;
    }

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

    public User getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(User preparedBy) {
        this.preparedBy = preparedBy;
    }
}
