package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.SpecialEquipmentWithdrawalDetail;

import java.math.BigDecimal;

/**
 * Created by lenovo on 6/15/2017.
 */
public class StockWithdrawalDetailDto {

    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity;
    private BigDecimal quantityReleased;
    private boolean isSpecialEquipment;
    private Integer itemStockId;
    private BigDecimal inventoryBalance;
    private BigDecimal rvBalance;
    private BigDecimal insertedQuantity;

    public StockWithdrawalDetailDto() {
    }

    public StockWithdrawalDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal quantityReleased, BigDecimal insertedQuantity, boolean isSpecialEquipment) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.quantityReleased = quantityReleased;
        this.insertedQuantity = insertedQuantity;
        this.isSpecialEquipment = isSpecialEquipment;
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

    public BigDecimal getQuantityReleased() {
        return quantityReleased;
    }

    public void setQuantityReleased(BigDecimal quantityReleased) {
        this.quantityReleased = quantityReleased;
    }

    public boolean isSpecialEquipment() {
        return isSpecialEquipment;
    }

    public void setIsSpecialEquipment(boolean specialEquipment) {
        isSpecialEquipment = specialEquipment;
    }

    public BigDecimal getInsertedQuantity() {
        return insertedQuantity;
    }

    public void setInsertedQuantity(BigDecimal insertedQuantity) {
        this.insertedQuantity = insertedQuantity;
    }

    public Integer getItemStockId() {
        return itemStockId;
    }

    public void setItemStockId(Integer itemStockId) {
        this.itemStockId = itemStockId;
    }

    public BigDecimal getInventoryBalance() {
        return inventoryBalance;
    }

    public void setInventoryBalance(BigDecimal inventoryBalance) {
        this.inventoryBalance = inventoryBalance;
    }

    public BigDecimal getRvBalance() {
        return rvBalance;
    }

    public void setRvBalance(BigDecimal rvBalance) {
        this.rvBalance = rvBalance;
    }
}
