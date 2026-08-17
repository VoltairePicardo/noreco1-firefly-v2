package com.noreco1.fireflyv2.controller.response.reports;

import java.math.BigDecimal;


public class QuotationDetail {
    private String description;
    private Integer id;
    private BigDecimal quantity;
    private Boolean isAvailable;
    private String supplier1;
    private String supplier2;
    private String supplier3;
    private String supplier4;

    private Integer purchaseRequestDetailId;

    public QuotationDetail() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getSupplier1() {
        return supplier1;
    }

    public void setSupplier1(String supplier1) {
        this.supplier1 = supplier1;
    }

    public String getSupplier2() {
        return supplier2;
    }

    public void setSupplier2(String supplier2) {
        this.supplier2 = supplier2;
    }

    public String getSupplier3() {
        return supplier3;
    }

    public void setSupplier3(String supplier3) {
        this.supplier3 = supplier3;
    }

    public Integer getPurchaseRequestDetailId() {
        return purchaseRequestDetailId;
    }

    public void setPurchaseRequestDetailId(Integer purchaseRequestDetailId) {
        this.purchaseRequestDetailId = purchaseRequestDetailId;
    }

    public String getSupplier4() {
        return supplier4;
    }

    public void setSupplier4(String supplier4) {
        this.supplier4 = supplier4;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
