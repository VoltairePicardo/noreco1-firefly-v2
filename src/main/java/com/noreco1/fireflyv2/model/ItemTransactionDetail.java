package com.noreco1.fireflyv2.model;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ItemTransactionDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_transactionId")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_itemId")
    private Item item;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal unitCost;

    @Column
    private BigDecimal totalCost;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_inventoryLocationId", nullable = true, columnDefinition = "0")
    private InventoryLocation inventoryLocation;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_itemStockId", nullable = true, columnDefinition = "0")
    private ItemStock itemStock;

    @Column
    private BigDecimal adjustment;

    @Column
    private BigDecimal quantityReleased;

    @Column
    private BigDecimal quantityReceived;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_memorandumReceiptDetailId", nullable = true, columnDefinition = "0")
    private MemorandumReceiptDetail memorandumReceiptDetail;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="FK_returnMemorandumReceiptDetailId", nullable = true, columnDefinition = "0")
    private ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail;

    @Transient
    private BigDecimal oldQuantity;

    @Transient
    private boolean isMRTE;

    private Boolean isUsable;

    private Boolean deductFromStock;

    @Column
    private BigDecimal deliveredQuantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_newItemId")
    private Item newItem;

    public ItemTransactionDetail(Transaction transaction, Item item, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, MemorandumReceiptDetail memorandumReceiptDetail, ReturnMemorandumReceiptDetail returnMemorandumReceiptDetail) {
        this.transaction = transaction;
        this.item = item;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.memorandumReceiptDetail = memorandumReceiptDetail;
        this.returnMemorandumReceiptDetail = returnMemorandumReceiptDetail;
    }

    public ItemTransactionDetail(Transaction transaction, Item item, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, InventoryLocation inventoryLocation, ItemStock itemStock, BigDecimal adjustment) {
        this.transaction = transaction;
        this.item = item;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocation = inventoryLocation;
        this.itemStock = itemStock;
        this.adjustment = adjustment;
    }

    public ItemTransactionDetail(Transaction transaction, Item item, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, InventoryLocation inventoryLocation, ItemStock itemStock, BigDecimal adjustment, BigDecimal quantityReleased) {
        this.transaction = transaction;
        this.item = item;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocation = inventoryLocation;
        this.itemStock = itemStock;
        this.adjustment = adjustment;
        this.quantityReleased = quantityReleased;
    }

    public ItemTransactionDetail(Transaction transaction, Item item, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, InventoryLocation inventoryLocation, BigDecimal adjustment, BigDecimal quantityReceived, ItemStock itemStock) {
        this.transaction = transaction;
        this.item = item;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocation = inventoryLocation;
        this.itemStock = itemStock;
        this.adjustment = adjustment;
        this.quantityReceived = quantityReceived;
    }

    public ItemTransactionDetail(Transaction transaction, Item item, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, InventoryLocation inventoryLocation, ItemStock itemStock, Boolean deductFromStock, BigDecimal deliveredQuantity, Item newItem) {
        this.transaction = transaction;
        this.item = item;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.inventoryLocation = inventoryLocation;
        this.itemStock = itemStock;
        this.deductFromStock = deductFromStock;
        this.deliveredQuantity = deliveredQuantity;
        this.newItem = newItem;
    }

    public ItemTransactionDetailDto toDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost());
    }

    public ItemTransactionDetailDto toAdjustmentDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost(), getInventoryLocation() == null ? null : getInventoryLocation().getId(), getItemStock().getId(), getAdjustment(), getInventoryLocation() == null ? null : getInventoryLocation().getDescription());
    }

    public ItemTransactionDetailDto toMSTDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost(), getInventoryLocation() == null ? null : getInventoryLocation().getId(), getItemStock() == null ? null : getItemStock().getId(), getMemorandumReceiptDetail(), getOldQuantity(), isMRTE(), getIsUsable());
    }

    public ItemTransactionDetailDto toIFRDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getInventoryLocation() == null ? null : getInventoryLocation().getId(), getItemStock() == null ? null : getItemStock().getId(), getDeductFromStock(), getDeliveredQuantity(), getNewItem() != null ? getNewItem().getId() : null, getNewItem() != null ? getNewItem().getDescription() : null);
    }

    public ItemTransactionDetailDto toReleaseDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost(), getInventoryLocation() == null ? null : getInventoryLocation().getId(), getItemStock().getId(), getQuantityReleased(), getItem().getInventoryCategory().getId());
    }

    public ItemTransactionDetailDto toReceiveDto() {
        return new ItemTransactionDetailDto(getItem().getId(), getItem().getCode(), getItem().getUnit().getId(), getItem().getUnit().getCode(), getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost(), getInventoryLocation() == null ? null : getInventoryLocation().getId(), getItemStock().getId(), getQuantityReleased(), getQuantityReceived());
    }

}
