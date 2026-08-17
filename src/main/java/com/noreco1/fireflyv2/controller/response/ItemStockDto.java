package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.InventoryLocation;
import com.noreco1.fireflyv2.model.Item;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

public class ItemStockDto implements Serializable {

    private Integer id;
    private Item item;
    private InventoryLocation inventoryLocation;
    private BigDecimal totalQuantity;
    private BigDecimal totalItemCost;
    private Date createdAt;
    private Date updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public InventoryLocation getInventoryLocation() {
        return inventoryLocation;
    }

    public void setInventoryLocation(InventoryLocation inventoryLocation) {
        this.inventoryLocation = inventoryLocation;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalItemCost() {
        return totalItemCost;
    }

    public void setTotalItemCost(BigDecimal totalItemCost) {
        this.totalItemCost = totalItemCost;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
