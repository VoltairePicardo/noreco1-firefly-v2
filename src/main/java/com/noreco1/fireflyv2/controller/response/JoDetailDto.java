package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by Personal on 6/18/2015.
 */
public class JoDetailDto {
    private Integer id;
    private Integer jobOrderId;
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
    private BigDecimal acceptedAmount = BigDecimal.ZERO;
    private String joDescription;
    private String rvDescription;
    private Integer itemId;

    public JoDetailDto() {}

    public JoDetailDto(Integer id, Integer jobOrderId, Integer rvDetailId, String itemCode, String unitCode, String itemDescription,
                       BigDecimal quantity, BigDecimal vat, BigDecimal discount, BigDecimal unitPrice, BigDecimal itemAmount,
                       BigDecimal rvdQuantity, BigDecimal acceptedAmount, String joDescription, String rvDescription,
                       Integer itemId) {
        this.id = id;
        this.jobOrderId = jobOrderId;
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
        this.acceptedAmount = acceptedAmount;
        this.joDescription = joDescription;
        this.rvDescription = rvDescription;
        this.itemId = itemId;
    }

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

    public Integer getJobOrderId() {
        return jobOrderId;
    }

    public void setJobOrderId(Integer jobOrderId) {
        this.jobOrderId = jobOrderId;
    }

    public BigDecimal getRvdQuantity() {
        return rvdQuantity;
    }

    public void setRvdQuantity(BigDecimal rvdQuantity) {
        this.rvdQuantity = rvdQuantity;
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

    public String getRvDescription() {
        return rvDescription;
    }

    public void setRvDescription(String rvDescription) {
        this.rvDescription = rvDescription;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
}
