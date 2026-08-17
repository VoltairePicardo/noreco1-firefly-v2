package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Personal on 4/29/2015.
 */
public class RvDetailDto {
    private Integer id;
    private Integer rvId;
    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity = BigDecimal.ZERO;
    private String joDescription;
    private String rvNumber;
    private Date rvDate;
    private BigDecimal poQuantity = BigDecimal.ZERO;
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;
    private BigDecimal remainingQuantity = BigDecimal.ZERO;
    private String rvPurpose;
    private Integer quotationId;
    private String itemGroupName;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;
    private Integer itemGroup;
    private String requestedBy = "";
    private Date deliveryDate;
    private Integer inventoryCategoryId;

    public RvDetailDto() {}

    public RvDetailDto(Integer id, Integer rvId, Integer itemId, String itemCode, Integer unitId, String unitCode,
                       String itemDescription, BigDecimal quantity, String joDescription, String rvNumber, Date rvDate,
                       BigDecimal poQuantity, BigDecimal acceptedQuantity, BigDecimal remainingQuantity,
                       String rvPurpose, Integer itemGroup, String itemGroupName, BigDecimal unitPrice, BigDecimal vat,
                       Integer quotationId, String requestedBy) {
        this.id = id;
        this.rvId = rvId;
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.joDescription = joDescription;
        this.rvNumber = rvNumber;
        this.rvDate = rvDate;
        this.poQuantity = poQuantity;
        this.acceptedQuantity = acceptedQuantity;
        this.remainingQuantity = remainingQuantity;
        this.rvPurpose = rvPurpose;
        this.itemGroup = itemGroup;
        this.itemGroupName = itemGroupName;
        this.unitPrice = unitPrice;
        this.vat = vat;
        this.quotationId = quotationId;
        this.requestedBy = requestedBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
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

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public Integer getRvId() {
        return rvId;
    }

    public void setRvId(Integer rvId) {
        this.rvId = rvId;
    }

    public String getJoDescription() {
        return joDescription;
    }

    public void setJoDescription(String joDescription) {
        this.joDescription = joDescription;
    }

    public String getRvNumber() {
        return rvNumber;
    }

    public void setRvNumber(String rvNumber) {
        this.rvNumber = rvNumber;
    }

    public Date getRvDate() {
        return rvDate;
    }

    public void setRvDate(Date rvDate) {
        this.rvDate = rvDate;
    }

    public BigDecimal getPoQuantity() {
        return poQuantity;
    }

    public void setPoQuantity(BigDecimal poQuantity) {
        this.poQuantity = poQuantity;
    }

    public BigDecimal getAcceptedQuantity() {
        return acceptedQuantity;
    }

    public void setAcceptedQuantity(BigDecimal acceptedQuantity) {
        this.acceptedQuantity = acceptedQuantity;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(BigDecimal remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getRvPurpose() {
        return rvPurpose;
    }

    public void setRvPurpose(String rvPurpose) {
        this.rvPurpose = rvPurpose;
    }

    public Integer getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(Integer itemGroup) {
        this.itemGroup = itemGroup;
    }

    public String getItemGroupName() {
        return itemGroupName;
    }

    public void setItemGroupName(String itemGroupName) {
        this.itemGroupName = itemGroupName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public Integer getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Integer quotationId) {
        this.quotationId = quotationId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Integer getInventoryCategoryId() {
        return inventoryCategoryId;
    }

    public void setInventoryCategoryId(Integer inventoryCategoryId) {
        this.inventoryCategoryId = inventoryCategoryId;
    }
}
