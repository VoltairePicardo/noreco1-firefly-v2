package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TSI Admin on 4/23/2015.
 */
public class ApvListDto {
    private Integer id;
    private Integer transId;
    private String localCode;
    private Integer supplierAccountNo;
    private String supplier;
    private BigDecimal amount;
    private String particulars;
    private Date date;
    private String status;
    private Boolean enableCheckBox;
    private Boolean selected;
    private String documentCode;

    public ApvListDto() {}

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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSupplierAccountNo() {
        return supplierAccountNo;
    }

    public void setSupplierAccountNo(Integer supplierAccountNo) {
        this.supplierAccountNo = supplierAccountNo;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public Boolean getEnableCheckBox() {
        return enableCheckBox;
    }

    public void setEnableCheckBox(Boolean enableCheckBox) {
        this.enableCheckBox = enableCheckBox;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }
}
