package com.noreco1.fireflyv2.controller.response;

import java.util.Date;

public class QuotationListDto {

    private Integer id;
    private String code;
    private Date date;
    private String description;
    private String requisitionVoucherCode;
    private String documentStatus;
    private String preparedBy;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequisitionVoucherCode() {
        return requisitionVoucherCode;
    }

    public void setRequisitionVoucherCode(String requisitionVoucherCode) {
        this.requisitionVoucherCode = requisitionVoucherCode;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getPreparedBy() {
        return preparedBy;
    }

    public void setPreparedBy(String preparedBy) {
        this.preparedBy = preparedBy;
    }
}
