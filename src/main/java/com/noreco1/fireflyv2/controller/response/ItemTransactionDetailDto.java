package com.noreco1.fireflyv2.controller.response;

import com.noreco1.fireflyv2.model.MemorandumReceiptDetail;
import com.noreco1.fireflyv2.model.ReturnMemorandumReceiptDetail;
import com.noreco1.fireflyv2.model.SpecialEquipment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ItemTransactionDetailDto {

    private Integer id;
    private Integer itemId;
    private String itemCode;
    private Integer unitId;
    private String unitCode;
    private String itemDescription;
    private String searchText;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal quantityReleased;
    private BigDecimal quantityReceived;
    private BigDecimal releaseQuantity;
    private BigDecimal receiveQuantity;
    private BigDecimal quantityOrdered;
    private Integer inventoryLocationId;
    private Integer itemStockId;
    private BigDecimal adjustment;
    private Integer inventoryCategoryId;
    private MemorandumReceiptDetail memorandumReceiptDetail;
    private ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail;
    private BigDecimal oldQuantity;
    private boolean isRMRTE;
    private Boolean isUsable;
    private Boolean deductFromStock;
    private Integer newItemId;
    private BigDecimal deliveredQuantity;
    private String newItemDescription;
    private List<SpecialEquipment> serialNumbers = new ArrayList<>();

    public ItemTransactionDetailDto() {
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, String searchText, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, BigDecimal quantityReleased, BigDecimal releaseQuantity, BigDecimal quantityOrdered, Integer inventoryLocationId, Integer itemStockId, BigDecimal adjustment, MemorandumReceiptDetail memorandumReceiptDetail, ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail, BigDecimal oldQuantity, boolean isRMRTE) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.searchText = searchText;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.quantityReleased = quantityReleased;
        this.releaseQuantity = releaseQuantity;
        this.quantityOrdered = quantityOrdered;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.adjustment = adjustment;
        this.memorandumReceiptDetail = memorandumReceiptDetail;
        this.returnMemorandumReceiptDetail = returnMemorandumReceiptDetail;
        this.oldQuantity = oldQuantity;
        this.isRMRTE = isRMRTE;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, BigDecimal quantityReleased, BigDecimal releaseQuantity, BigDecimal quantityOrdered) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.quantityReleased = quantityReleased;
        this.releaseQuantity = releaseQuantity;
        this.quantityOrdered = quantityOrdered;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, Integer inventoryLocationId, Integer itemStockId, BigDecimal adjustment, String searchText) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.adjustment = adjustment;
        this.searchText = searchText;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, Integer inventoryLocationId, Integer itemStockId, MemorandumReceiptDetail memorandumReceiptDetail, BigDecimal oldQuantity, boolean isRMRTE, Boolean isUsable) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.memorandumReceiptDetail = memorandumReceiptDetail;
        this.oldQuantity = oldQuantity;
        this.isRMRTE = isRMRTE;
        this.isUsable = isUsable;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, Integer inventoryLocationId, Integer itemStockId, BigDecimal quantityReleased, Integer inventoryCategoryId) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.quantityReleased = quantityReleased;
        this.inventoryCategoryId = inventoryCategoryId;
    }

    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, Integer inventoryLocationId, Integer itemStockId, BigDecimal quantityReleased, BigDecimal quantityReceived) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.quantityReleased = quantityReleased;
        this.quantityReceived = quantityReceived;
    }

    //itemsForRepair
    public ItemTransactionDetailDto(Integer itemId, String itemCode, Integer unitId, String unitCode, String itemDescription, BigDecimal quantity, Integer inventoryLocationId, Integer itemStockId, Boolean deductFromStock, BigDecimal deliveredQuantity, Integer newItemId, String newItemDescription) {
        this.itemId = itemId;
        this.itemCode = itemCode;
        this.unitId = unitId;
        this.unitCode = unitCode;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.inventoryLocationId = inventoryLocationId;
        this.itemStockId = itemStockId;
        this.deductFromStock = deductFromStock;
        this.deliveredQuantity = deliveredQuantity;
        this.newItemId = newItemId;
        this.newItemDescription = newItemDescription;
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

    public BigDecimal getQuantityReleased() {
        return quantityReleased;
    }

    public void setQuantityReleased(BigDecimal quantityReleased) {
        this.quantityReleased = quantityReleased;
    }

    public BigDecimal getReleaseQuantity() {
        return releaseQuantity;
    }

    public void setReleaseQuantity(BigDecimal releaseQuantity) {
        this.releaseQuantity = releaseQuantity;
    }

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getInventoryLocationId() {
        return inventoryLocationId;
    }

    public void setInventoryLocationId(Integer inventoryLocationId) {
        this.inventoryLocationId = inventoryLocationId;
    }

    public Integer getItemStockId() {
        return itemStockId;
    }

    public void setItemStockId(Integer itemStockId) {
        this.itemStockId = itemStockId;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Integer getInventoryCategoryId() {
        return inventoryCategoryId;
    }

    public void setInventoryCategoryId(Integer inventoryCategoryId) {
        this.inventoryCategoryId = inventoryCategoryId;
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(BigDecimal quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getReceiveQuantity() {
        return receiveQuantity;
    }

    public void setReceiveQuantity(BigDecimal receiveQuantity) {
        this.receiveQuantity = receiveQuantity;
    }

    public MemorandumReceiptDetail getMemorandumReceiptDetail() {
        return memorandumReceiptDetail;
    }

    public void setMemorandumReceiptDetail(MemorandumReceiptDetail memorandumReceiptDetail) {
        this.memorandumReceiptDetail = memorandumReceiptDetail;
    }

    public ReturnMemorandumReceiptDetail getReturnMemorandumReceiptDetail() {
        return returnMemorandumReceiptDetail;
    }

    public void setReturnMemorandumReceiptDetail(ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail) {
        this.returnMemorandumReceiptDetail = returnMemorandumReceiptDetail;
    }

    public BigDecimal getOldQuantity() {
        return oldQuantity;
    }

    public void setOldQuantity(BigDecimal oldQuantity) {
        this.oldQuantity = oldQuantity;
    }

    public boolean isRMRTE() {
        return isRMRTE;
    }

    public void setRMRTE(boolean RMRTE) {
        isRMRTE = RMRTE;
    }

    public Boolean getIsUsable() {
        return isUsable;
    }

    public void setIsUsable(Boolean isUsable) {
        this.isUsable = isUsable;
    }

    public Boolean getDeductFromStock() {
        return deductFromStock;
    }

    public void setDeductFromStock(Boolean deductFromStock) {
        this.deductFromStock = deductFromStock;
    }

    public Integer getNewItemId() {
        return newItemId;
    }

    public void setNewItemId(Integer newItemId) {
        this.newItemId = newItemId;
    }

    public BigDecimal getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(BigDecimal deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public String getNewItemDescription() {
        return newItemDescription;
    }

    public void setNewItemDescription(String newItemDescription) {
        this.newItemDescription = newItemDescription;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<SpecialEquipment> getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(List<SpecialEquipment> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }
}
