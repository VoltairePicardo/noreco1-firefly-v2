package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.MemorandumReceiptDetail;
import com.noreco1.fireflyv2.model.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class InventoryDocumentDto {
    private String name;
    private Date date;
    private String code;
    private String purpose;
    private String createdBy;
    private User createdByUser;
    private String departmentName;
    private Integer transId;
    private Integer inventoryCategoryTypeId;
    private ArrayList<ItemTransactionDetailDto> details;
    private ArrayList<StockWithdrawalDetailDto> withdrawalDetails;
    private BigDecimal grandTotal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public ArrayList<ItemTransactionDetailDto> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<ItemTransactionDetailDto> details) {
        this.details = details;
    }

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public ArrayList<StockWithdrawalDetailDto> getWithdrawalDetails() {
        return withdrawalDetails;
    }

    public void setWithdrawalDetails(ArrayList<StockWithdrawalDetailDto> withdrawalDetails) {
        this.withdrawalDetails = withdrawalDetails;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public Integer getInventoryCategoryTypeId() {
        return inventoryCategoryTypeId;
    }

    public void setInventoryCategoryTypeId(Integer inventoryCategoryTypeId) {
        this.inventoryCategoryTypeId = inventoryCategoryTypeId;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }
}
