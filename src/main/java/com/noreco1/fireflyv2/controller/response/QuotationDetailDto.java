package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

public class QuotationDetailDto {

    private Integer id;
    private Integer quotationId;
    private Integer rvDetailId;
    private String itemCode;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String rvNumber;
    private BigDecimal priceSupplier1 = BigDecimal.ZERO;
    private BigDecimal priceSupplier2 = BigDecimal.ZERO;
    private BigDecimal priceSupplier3 = BigDecimal.ZERO;
    private BigDecimal priceSupplier4 = BigDecimal.ZERO;
    private Boolean awardedToSupplier1;
    private Boolean awardedToSupplier2;
    private Boolean awardedToSupplier3;
    private Boolean awardedToSupplier4;
    private Boolean available;

    public QuotationDetailDto(Integer id, Integer quotationId, Integer rvDetailId, String itemCode, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitPrice, String rvNumber, BigDecimal priceSupplier1, BigDecimal priceSupplier2, BigDecimal priceSupplier3, BigDecimal priceSupplier4, Boolean awardedToSupplier1, Boolean awardedToSupplier2, Boolean awardedToSupplier3, Boolean awardedToSupplier4, Boolean available) {
        this.id = id;
        this.quotationId = quotationId;
        this.rvDetailId = rvDetailId;
        this.itemCode = itemCode;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.rvNumber = rvNumber;
        this.priceSupplier1 = priceSupplier1;
        this.priceSupplier2 = priceSupplier2;
        this.priceSupplier3 = priceSupplier3;
        this.priceSupplier4 = priceSupplier4;
        this.awardedToSupplier1 = awardedToSupplier1;
        this.awardedToSupplier2 = awardedToSupplier2;
        this.awardedToSupplier3 = awardedToSupplier3;
        this.awardedToSupplier4 = awardedToSupplier4;
        this.available = available;
    }

    public QuotationDetailDto() {
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

    public Integer getRvDetailId() {
        return rvDetailId;
    }

    public void setRvDetailId(Integer rvDetailId) {
        this.rvDetailId = rvDetailId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getRvNumber() {
        return rvNumber;
    }

    public void setRvNumber(String rvNumber) {
        this.rvNumber = rvNumber;
    }

    public BigDecimal getPriceSupplier1() {
        return priceSupplier1;
    }

    public void setPriceSupplier1(BigDecimal priceSupplier1) {
        this.priceSupplier1 = priceSupplier1;
    }

    public BigDecimal getPriceSupplier2() {
        return priceSupplier2;
    }

    public void setPriceSupplier2(BigDecimal priceSupplier2) {
        this.priceSupplier2 = priceSupplier2;
    }

    public BigDecimal getPriceSupplier3() {
        return priceSupplier3;
    }

    public void setPriceSupplier3(BigDecimal priceSupplier3) {
        this.priceSupplier3 = priceSupplier3;
    }

    public void setPriceSupplier4(BigDecimal priceSupplier4) {
        this.priceSupplier4 = priceSupplier4;
    }

    public BigDecimal getPriceSupplier4() {
        return priceSupplier4;
    }

    public Boolean getAwardedToSupplier1() {
        return awardedToSupplier1;
    }

    public void setAwardedToSupplier1(Boolean awardedToSupplier1) {
        this.awardedToSupplier1 = awardedToSupplier1;
    }

    public Boolean getAwardedToSupplier2() {
        return awardedToSupplier2;
    }

    public void setAwardedToSupplier2(Boolean awardedToSupplier2) {
        this.awardedToSupplier2 = awardedToSupplier2;
    }

    public Boolean getAwardedToSupplier3() {
        return awardedToSupplier3;
    }

    public void setAwardedToSupplier3(Boolean awardedToSupplier3) {
        this.awardedToSupplier3 = awardedToSupplier3;
    }

    public Boolean getAwardedToSupplier4() {
        return awardedToSupplier4;
    }

    public void setAwardedToSupplier4(Boolean awardedToSupplier4) {
        this.awardedToSupplier4 = awardedToSupplier4;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
