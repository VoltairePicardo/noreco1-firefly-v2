package com.noreco1.fireflyv2.model;

import lombok.*;

import com.noreco1.fireflyv2.controller.response.ItemTransactionDetailDto;

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
public class StockTransactionDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_stockTransactionId")
    private StockTransaction stockTransaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_itemStockId")
    private ItemStock itemStock;

    @Column
    private BigDecimal quantity;

    @Column
    private BigDecimal unitCost;

    @Column
    private BigDecimal totalCost;

    @Column
    private BigDecimal vat;

    @Column
    private BigDecimal itemStockBalance;

    @Column
    private BigDecimal itemStockAmountBalance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_inventoryLocationId")
    private InventoryLocation inventoryLocation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_itemStockDetailId")
    private ItemStockDetail itemStockDetail;

    @Column
    private Integer type;

    public StockTransactionDetail(StockTransaction stockTransaction, ItemStock itemStock, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, BigDecimal vat, BigDecimal itemStockBalance, BigDecimal itemStockAmountBalance, InventoryLocation inventoryLocation, Integer type, ItemStockDetail itemStockDetail) {
        this.stockTransaction = stockTransaction;
        this.itemStock = itemStock;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.vat = vat;
        this.itemStockBalance = itemStockBalance;
        this.itemStockAmountBalance = itemStockAmountBalance;
        this.inventoryLocation = inventoryLocation;
        this.type = type;
        this.itemStockDetail = itemStockDetail;
    }

    public ItemTransactionDetailDto toDto() {
        return new ItemTransactionDetailDto(getItemStock().getItem().getId(), getItemStock().getItem().getCode(), getItemStock().getItem().getUnit().getId(), getItemStock().getItem().getUnit().getCode(), getItemStock().getItem().getDescription(), getQuantity(), getUnitCost(), getTotalCost());
    }

}
