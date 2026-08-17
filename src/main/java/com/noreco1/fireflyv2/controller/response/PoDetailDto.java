package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.Brand;

import java.math.BigDecimal;

/**
 * Created by Personal on 5/15/2015.
 */
public class PoDetailDto {
    private Integer id;
    private Integer purchaseOrderId;
    private Integer itemId;
    private Integer rvDetailId;
    private String itemCode;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal itemAmount = BigDecimal.ZERO;
    private BigDecimal rvdQuantity = BigDecimal.ZERO;
    private BigDecimal remainingQuantity = BigDecimal.ZERO;
    private BigDecimal poQuantity = BigDecimal.ZERO;
    private String requisitionVoucherCode;
    private Brand brand;
    private BigDecimal sentForTestingQuantity;
    private BigDecimal deliveredQuantity;

    public PoDetailDto(Integer id, Integer purchaseOrderId, Integer itemId, Integer rvDetailId, String itemCode,
                       String unitCode, String itemDescription, BigDecimal quantity, BigDecimal vat, BigDecimal discount,
                       BigDecimal unitPrice, BigDecimal itemAmount, BigDecimal rvdQuantity, BigDecimal remainingQuantity,
                       BigDecimal poQuantity, String requisitionVoucherCode, Brand brand, BigDecimal sentForTestingQuantity, BigDecimal deliveredQuantity) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.itemId = itemId;
        this.rvDetailId = rvDetailId;
        this.itemCode = itemCode;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.vat = vat;
        this.discount = discount;
        this.unitPrice = unitPrice;
        this.itemAmount = itemAmount;
        this.rvdQuantity = rvdQuantity;
        this.remainingQuantity = remainingQuantity;
        this.poQuantity = poQuantity;
        this.brand = brand;
        this.requisitionVoucherCode = requisitionVoucherCode;
        this.sentForTestingQuantity = sentForTestingQuantity;
        this.deliveredQuantity = deliveredQuantity;
    }

    public PoDetailDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public BigDecimal getRvdQuantity() {
        return rvdQuantity;
    }

    public void setRvdQuantity(BigDecimal rvdQuantity) {
        this.rvdQuantity = rvdQuantity;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public BigDecimal getPoQuantity() {
        return poQuantity;
    }

    public void setPoQuantity(BigDecimal poQuantity) {
        this.poQuantity = poQuantity;
    }

    public String getRequisitionVoucherCode() {
        return requisitionVoucherCode;
    }

    public void setRequisitionVoucherCode(String requisitionVoucherCode) {
        this.requisitionVoucherCode = requisitionVoucherCode;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public BigDecimal getSentForTestingQuantity() {
        return sentForTestingQuantity;
    }

    public void setSentForTestingQuantity(BigDecimal sentForTestingQuantity) {
        this.sentForTestingQuantity = sentForTestingQuantity;
    }

    public BigDecimal getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(BigDecimal deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }
}