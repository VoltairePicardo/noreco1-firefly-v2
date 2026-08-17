package com.noreco1.fireflyv2.controller.response;

import java.util.Date;

/**
 * Created by Personal on 11/12/2015.
 */
public class TemporaryBatchDto {
    private Integer id;
    private Integer documentTypeId;
    private Date date;
    private Boolean voucherCreated;
    private String remarks;
    private Integer transId;

    public TemporaryBatchDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Integer documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getVoucherCreated() {
        return voucherCreated;
    }

    public void setVoucherCreated(Boolean voucherCreated) {
        this.voucherCreated = voucherCreated;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }
}
