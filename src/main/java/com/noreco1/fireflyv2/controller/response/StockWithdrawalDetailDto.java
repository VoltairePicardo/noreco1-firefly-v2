package com.noreco1.fireflyv2.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by lenovo on 6/15/2017.
 */
@Getter
@Setter
public class StockWithdrawalDetailDto {

    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity;
    private BigDecimal quantityReleased;

    @JsonProperty("isSpecialEquipment")
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

}
