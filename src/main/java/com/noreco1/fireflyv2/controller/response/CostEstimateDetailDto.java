package com.noreco1.fireflyv2.controller.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CostEstimateDetailDto {
    private Integer id;
    private Integer costEstimateId;
    private Integer assemblyUnitId;
    private String assemblyCode;
    private String projectCode;
    private Integer itemStockId;
    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private Integer category;
    private String unitCode;
    private String itemDescription;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal inventoryCost;
    private BigDecimal markUp;
    private BigDecimal inventoryQty;
    private BigDecimal unitQty;
    private List<Map> assemblyUnitIds = new ArrayList<>();

    public CostEstimateDetailDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCostEstimateId() {
        return costEstimateId;
    }

    public void setCostEstimateId(Integer costEstimateId) {
        this.costEstimateId = costEstimateId;
    }

    public Integer getAssemblyUnitId() {
        return assemblyUnitId;
    }

    public void setAssemblyUnitId(Integer assemblyUnitId) {
        this.assemblyUnitId = assemblyUnitId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getInventoryCost() {
        return inventoryCost;
    }

    public void setInventoryCost(BigDecimal inventoryCost) {
        this.inventoryCost = inventoryCost;
    }

    public BigDecimal getMarkUp() {
        return markUp;
    }

    public void setMarkUp(BigDecimal markUp) {
        this.markUp = markUp;
    }

    public BigDecimal getInventoryQty() {
        return inventoryQty;
    }

    public void setInventoryQty(BigDecimal inventoryQty) {
        this.inventoryQty = inventoryQty;
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

    public String getAssemblyCode() {
        return assemblyCode;
    }

    public void setAssemblyCode(String assemblyCode) {
        this.assemblyCode = assemblyCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public Integer getItemStockId() {
        return itemStockId;
    }

    public void setItemStockId(Integer itemStockId) {
        this.itemStockId = itemStockId;
    }

    public BigDecimal getUnitQty() {
        return unitQty;
    }

    public void setUnitQty(BigDecimal unitQty) {
        this.unitQty = unitQty;
    }

    public List<Map> getAssemblyUnitIds() {
        return assemblyUnitIds;
    }

    public void setAssemblyUnitIds(List<Map> assemblyUnitIds) {
        this.assemblyUnitIds = assemblyUnitIds;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }
}
