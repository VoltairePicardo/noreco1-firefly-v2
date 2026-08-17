package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class QuotationItemDto {

    private Integer id;
    private Integer quotationId;
    private Integer purchaseRequestDetailId;
    private Boolean isAvailable;

    private String rvNo;
    private String itemDescription;
    private BigDecimal quantity;
    private String unitCode;

    private List<QuotationItemDetailDto> details = new ArrayList<>();

    public QuotationItemDto() {
    }

    public QuotationItemDto(Integer id, Integer quotationId, Integer rvDetailId, Boolean isAvailable, String rvNo,
                            String itemDescription, BigDecimal quantity, List<QuotationItemDetailDto> details, String unitCode ) {
        this.id = id;
        this.quotationId = quotationId;
        this.purchaseRequestDetailId = purchaseRequestDetailId;
        this.isAvailable = isAvailable;
        this.rvNo = rvNo;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.details = details;
        this.unitCode = unitCode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Integer quotationId) {
        this.quotationId = quotationId;
    }

    public Integer getPurchaseRequestDetailId() {
        return purchaseRequestDetailId;
    }

    public void setPurchaseRequestDetailId(Integer purchaseRequestDetailId) {
        this.purchaseRequestDetailId = purchaseRequestDetailId;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public List<QuotationItemDetailDto> getDetails() {
        return details;
    }

    public void setDetails(List<QuotationItemDetailDto> details) {
        this.details = details;
    }

    public String getRvNo() {
        return rvNo;
    }

    public void setRvNo(String rvNo) {
        this.rvNo = rvNo;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
}
