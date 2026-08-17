package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by Personal on 7/7/2015.
 */
public class JoAcceptanceDetailDto {
    private Integer id;
    private Integer joAcceptanceId;
    private Integer joDetailId;
    private Integer rvDetailId;
    private String unitCode;
    private String itemDescription;
    private String joDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal itemAmount = BigDecimal.ZERO;
    private BigDecimal remainingAmount = BigDecimal.ZERO;
    private BigDecimal acceptedAmount = BigDecimal.ZERO;
    private BigDecimal adjustment = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;

    public JoAcceptanceDetailDto() {}

    public JoAcceptanceDetailDto(Integer id, Integer joDetailId, Integer joAcceptanceId, String joDescription, String unitCode,
                       String itemDescription, BigDecimal quantity, BigDecimal discount,
                       BigDecimal unitPrice, BigDecimal itemAmount, BigDecimal remainingAmount,
                       BigDecimal acceptedAmount, BigDecimal adjustment, BigDecimal netAmount) {
        this.id = id;
        this.joAcceptanceId = joAcceptanceId;
        this.joDetailId = joDetailId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.discount = discount;
        this.unitPrice = unitPrice;
        this.itemAmount = itemAmount;
        this.remainingAmount = remainingAmount;
        this.joDescription = joDescription;
        this.acceptedAmount = acceptedAmount;
        this.setAdjustment(adjustment);
        this.netAmount = netAmount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJoDetailId() {
        return joDetailId;
    }

    public void setJoDetailId(Integer joDetailId) {
        this.joDetailId = joDetailId;
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

    public Integer getJoAcceptanceId() {
        return joAcceptanceId;
    }

    public void setJoAcceptanceId(Integer joAcceptanceId) {
        this.joAcceptanceId = joAcceptanceId;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getJoDescription() {
        return joDescription;
    }

    public void setJoDescription(String joDescription) {
        this.joDescription = joDescription;
    }

    public BigDecimal getAcceptedAmount() {
        return acceptedAmount;
    }

    public void setAcceptedAmount(BigDecimal acceptedAmount) {
        this.acceptedAmount = acceptedAmount;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public Integer getRvDetailId() {
        return rvDetailId;
    }

    public void setRvDetailId(Integer rvDetailId) {
        this.rvDetailId = rvDetailId;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
}