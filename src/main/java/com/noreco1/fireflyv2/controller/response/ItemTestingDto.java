package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Tri-Nvent on 5/21/2020.
 */
public class ItemTestingDto {

    private Integer id;
    private Date date;
    private InventoryLocation inventoryLocation;
    private User createdBy;
    private List<ItemTestingDetail> itemTestingDetails;
    private Date createdAt;
    private Date updatedAt;
    private Supplier supplier;
    private PurchaseOrder purchaseOrder;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public InventoryLocation getInventoryLocation() {
        return inventoryLocation;
    }

    public void setInventoryLocation(InventoryLocation inventoryLocation) {
        this.inventoryLocation = inventoryLocation;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<ItemTestingDetail> getItemTestingDetails() {
        return itemTestingDetails;
    }

    public void setItemTestingDetails(List<ItemTestingDetail> itemTestingDetails) {
        this.itemTestingDetails = itemTestingDetails;
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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
}
