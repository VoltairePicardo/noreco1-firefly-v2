package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;

/**
 * Created by lenovo on 6/15/2017.
 */
public class StockReleaseDetailDto {

    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReleased;

    public StockReleaseDetailDto() {
    }

    public StockReleaseDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantityOrdered, BigDecimal quantityReleased) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantityOrdered = quantityOrdered;
        this.quantityReleased = quantityReleased;
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

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getQuantityReleased() {
        return quantityReleased;
    }

    public void setQuantityReleased(BigDecimal quantityReleased) {
        this.quantityReleased = quantityReleased;
    }
}
